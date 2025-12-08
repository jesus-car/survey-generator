# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Boot application that generates quiz questions from markdown documents using AI (Ollama) and manages user authentication with JWT. The application accepts markdown file uploads, uses a local LLM to generate structured quiz questions, and provides user registration/login functionality.

## Technology Stack

- **Java 25** with Spring Boot 3.5.8
- **Spring AI** (1.1.1) for LLM integration with Ollama
- **MongoDB** for data persistence
- **Spring Security** with JWT (jjwt 0.13.0) for authentication
- **Testcontainers** for integration testing
- **Lombok** for boilerplate reduction
- **SpringDoc OpenAPI** for API documentation

## Development Commands

### Build and Run

Build the project:
```bash
./mvnw clean install
```

Run the application:
```bash
./mvnw spring-boot:run
```

The application will start on the default port (8080).

### Testing

Run all tests:
```bash
./mvnw test
```

Run a specific test class:
```bash
./mvnw test -Dtest=SurveyGeneratorApplicationTests
```

Run tests with coverage:
```bash
./mvnw test jacoco:report
```

### Code Quality

Format code (if configured):
```bash
./mvnw spotless:apply
```

### API Documentation

Once running, access Swagger UI at:
```
http://localhost:8080/swagger-ui.html
```

## Architecture

### Hexagonal Architecture (Ports and Adapters)

The application follows hexagonal architecture principles with clear separation of concerns across two main modules:

```
document/
├── application/           # Core business logic (Application Layer)
│   ├── dto/              # Data Transfer Objects
│   │   └── response/     # API response models
│   ├── port/             # Port interfaces
│   │   └── output/       # Output ports (interfaces to external systems)
│   └── service/          # Application services (use cases)
└── infrastructure/        # Infrastructure Layer
    ├── input/            # Input adapters (controllers)
    │   └── controller/   # REST controllers
    └── output/           # Output adapters (external integrations)
        └── ai/           # AI integration adapter
            ├── adapter/  # Adapter implementations
            └── config/   # AI configuration

user/
├── application/           # User business logic
│   ├── dto/
│   │   ├── command/      # Request DTOs (UserLoginRequest, UserRegisterRequest)
│   │   └── response/     # Response DTOs (UserLoginResponse, UserRegisterResponse)
│   ├── mapper/           # UserMapper (DTO ↔ Model conversion)
│   ├── port/output/      # UserAccessDatabasePort interface
│   └── service/          # UserService (registration, login)
├── domain/
│   └── model/            # UserModel (domain entity)
└── infrastructure/
    ├── config/           # Security configuration (AuthConfiguration, JwtAuthorizationFilter)
    ├── input/controller/ # UserController (REST endpoints)
    └── output/database/  # Database adapters
        ├── adapter/      # UserAccessDatabaseAdapter (port implementation)
        ├── entity/       # MongoDB entities (UserEntity, RoleEntity)
        ├── mapper/       # UserEntityMapper (Entity ↔ Model conversion)
        └── repository/   # UserRepository (MongoRepository)
```

#### Layer Responsibilities

**Application Layer**:
- Contains business logic and use cases
- Defines port interfaces for external dependencies
- Independent of frameworks and external systems
- DTOs define contracts for API requests/responses
- Mappers convert between DTOs and domain models

**Domain Layer**:
- Pure domain models representing core business entities
- No dependencies on infrastructure or frameworks
- Resides between application and infrastructure layers

**Infrastructure Layer**:
- **Input Adapters**: REST controllers that receive HTTP requests
- **Output Adapters**: Implementations of output ports (database, AI, external services)
- **Configuration**: Framework-specific configurations (security, filters)
- **Database**: Entity classes, repositories, and entity-to-model mappers

#### Dependency Flow

- Controllers (input adapters) depend on Services
- Services depend on Port interfaces (not implementations)
- Adapters (output) implement Port interfaces
- Domain logic is isolated from infrastructure concerns
- Mappers exist at layer boundaries:
  - **Application Mappers**: DTO ↔ Domain Model (e.g., `UserMapper`)
  - **Infrastructure Mappers**: Entity ↔ Domain Model (e.g., `UserEntityMapper`)

### Key Components

#### Document Module

**Controllers (Input Adapters)**
- **DocumentController**: REST endpoint at `/api/v1/documents/upload`
  - Accepts multiple markdown files via multipart upload
  - Validates files using `FileValidator`
  - Delegates to `DocumentService` for processing

**Services (Application Layer)**
- **DocumentService**: Orchestrates quiz generation workflow
  1. Extracts content from uploaded files
  2. Validates content is not empty
  3. Calls AI processor to generate questions

- **FileContentProcessor**: Handles file content extraction and validation
  - Reads multipart files as UTF-8 strings
  - Validates content is not null/empty

**Ports (Interfaces)**
- **AiProcessorPort**: Output port interface for AI question generation
  - Defines contract: `QuizResponse generateQuestions(String markdownContent)`
  - Implemented by `AiProcessorAdapter`

**Adapters (Output)**
- **AiProcessorAdapter**: Integrates with Ollama LLM via Spring AI
  - Uses `ChatClient` configured with system prompt and temperature settings
  - Loads prompt template from `classpath:/static/prompt/GenerateQuizPrompt`
  - Converts AI response to `QuizResponse` using `BeanOutputConverter`
  - Configured to generate 5 questions with 5 options each

**Configuration**
- **AiConfig**: Configures Spring AI `ChatClient`
  - System prompt: `classpath:/static/prompt/ExpertSystemPrompt`
  - Temperature: 0.4 (more deterministic responses)
  - Top-P: 0.9

#### User Module

**Controllers (Input Adapters)**
- **UserController**: REST endpoints at `/api/v1/users`
  - `POST /register`: User registration
  - `POST /login`: User authentication

**Services (Application Layer)**
- **UserService**: Handles user authentication and registration
  - `registerUser()`: Validates username/email uniqueness, encrypts password, creates user with USER role
  - `login()`: Authenticates credentials using Spring Security AuthenticationManager, generates JWT token
  - Uses `UserMapper` to convert between DTOs and domain models

**Ports (Interfaces)**
- **UserAccessDatabasePort**: Database access interface
  - `existsByUsername(String username)`: Check username availability
  - `existsByEmail(String email)`: Check email availability
  - `save(UserModel userModel)`: Persist user
  - `findByUsername(String username)`: Retrieve user by username

**Adapters (Output)**
- **UserAccessDatabaseAdapter**: MongoDB implementation of UserAccessDatabasePort
  - Uses `UserRepository` (MongoRepository) for database operations
  - Uses `UserEntityMapper` to convert between `UserEntity` and `UserModel`
  - Implements all port methods by delegating to repository and mapping results

**Database Layer**
- **UserRepository**: Spring Data MongoDB repository interface
  - Extends `MongoRepository<UserEntity, String>`
  - Provides custom queries: `findByUsername`, `existsByUsername`, `existsByEmail`
- **UserEntity**: MongoDB document entity
  - Collection name: "users"
  - Fields: `id` (String, @Id), `username` (@Indexed unique), `email` (@Indexed unique), `password`, `roles` (Set<RoleEntity>)
- **RoleEntity**: Embedded role document with `id` and `role` fields

**Mappers**
- **UserMapper** (Application layer): DTO ↔ UserModel
  - `toUserModel(UserRegisterRequest)`: Converts registration request to model, encrypts password, sets default USER role
  - `toUserRegisterResponse(UserModel)`: Converts model to registration response
- **UserEntityMapper** (Infrastructure layer): UserEntity ↔ UserModel
  - `toModel(UserEntity)`: Converts entity to domain model, maps role entities to role strings
  - `toEntity(UserModel)`: Converts domain model to entity, maps role strings to role entities

### Validation Pattern

The application uses a custom `ValidatorResult<T>` wrapper for validation:
- **FileValidator**: Validates file uploads
  - Only `.md` (markdown) files allowed
  - Checks for null, empty files, and valid extensions
  - Returns `ValidatorResult<List<MultipartFile>>` or `ValidatorResult<MultipartFile>`

- **FileContentProcessor**: Validates extracted content
  - Ensures content is not null or empty
  - Returns `ValidatorResult<String>`

Controllers check `validationResult.isValid()` and return 400 Bad Request if invalid.

### DTO Structure

**QuizResponse**: Nested record structure for quiz data
- `statement`: Overall quiz description
- `questions`: List of Question records
  - `question`: Question text
  - `options`: QuestionOptions record
    - `options`: List of answer choices
    - `answer`: Correct answer

Uses Jackson annotations for JSON serialization with specific property ordering.

## Configuration

### Required Environment

**MongoDB**: Local instance or container
```yaml
spring.data.mongodb.uri: mongodb://localhost/test
spring.data.mongodb.database: test-webflux
```

**Ollama**: Local LLM server
```yaml
spring.ai.ollama.base-url: http://localhost:11434
spring.ai.ollama.chat.options.model: gemma3
```

Ensure Ollama is running with the `gemma3` model before starting the application:
```bash
ollama pull gemma3
ollama serve
```

### Application Properties

Configuration is in `src/main/resources/application.yml`.

## Testing

### Testcontainers Integration

Tests use Testcontainers for MongoDB:
- **TestcontainersConfiguration**: Provides MongoDB container with `@ServiceConnection`
- Automatically starts `mongo:latest` container during tests
- No manual database setup required for testing

### Running Tests

Tests require Docker to be running for Testcontainers.

Basic test:
```bash
./mvnw test
```

## API Endpoints

### User Authentication

#### POST /api/v1/users/register

Register a new user.

**Request**:
```json
{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "securePassword123"
}
```

**Response**: `200 OK`
```json
{
  "username": "johndoe",
  "email": "john@example.com"
}
```

**Errors**:
- `400 Bad Request`: Username or email already exists

#### POST /api/v1/users/login

Authenticate a user and receive JWT token.

**Request**:
```json
{
  "email": "john@example.com",
  "password": "securePassword123"
}
```

**Response**: `200 OK`
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Errors**:
- `401 Unauthorized`: Invalid credentials

### Document Processing

#### POST /api/v1/documents/upload

Upload markdown files to generate quiz questions.

**Request**:
- Method: POST
- Content-Type: multipart/form-data
- Parameter: `files` (multiple markdown files)

**Response**: `200 OK`
```json
[
  {
    "statement": "Quiz about the document content",
    "questions": [
      {
        "question": "What is...?",
        "options": {
          "options": ["Option 1", "Option 2", "Option 3", "Option 4", "Option 5"],
          "answer": "Option 1"
        }
      }
    ]
  }
]
```

**Error**: `400 Bad Request` if file validation fails

## Project Structure Notes

### Naming Conventions

- Package structure uses full domain-based naming: `com.surveygenerator.surveygenerator`
- Clear separation between `application` (business logic) and `infrastructure` (technical implementation)
- Use of `port` package for interface definitions following hexagonal architecture

### Utilities

- **ValidatorResult**: Generic wrapper for validation results with success/error states
- **FileValidator**: Service for validating uploaded files

## Security Configuration

### Spring Security Setup

The application uses Spring Security with JWT-based stateless authentication:

**AuthConfiguration**:
- CSRF disabled (stateless API)
- All requests currently permit all (`.anyRequest().permitAll()`)
- Session management: STATELESS
- JWT filter runs before `UsernamePasswordAuthenticationFilter`
- BCrypt password encoder for password hashing

**JwtAuthorizationFilter**:
- Extends `OncePerRequestFilter`
- Extracts JWT from `Authorization: Bearer <token>` header
- Validates token signature and expiration
- Loads user details and sets Spring Security authentication context
- Returns 401 Unauthorized with JSON error response for:
  - Invalid token signature
  - Expired token
  - Malformed token

**JwtUtils**:
- Generates JWT tokens with HS256 signing algorithm
- Token expiration: 24 hours (86400000ms)
- Claims: `authorities` (user roles), `email`, `subject` (username)
- Secret key: Auto-generated HS256 key (regenerated on each application restart)

**UserDetailsService**:
- Custom implementation loads users from MongoDB via `UserAccessDatabasePort`
- Converts `UserModel` roles to Spring Security `GrantedAuthority`

### Authentication Flow

1. User registers via `/api/v1/users/register`
   - Password encrypted with BCrypt
   - User saved to MongoDB with default "USER" role
2. User logs in via `/api/v1/users/login`
   - Spring Security authenticates credentials
   - JWT token generated and returned
3. Subsequent requests include JWT in Authorization header
4. `JwtAuthorizationFilter` validates token and sets authentication context

## Important Notes

### AI Integration

- The AI adapter uses Spring AI's `ChatClient` with structured output conversion
- Prompts are loaded from classpath resources in `src/main/resources/static/prompt/`
- The system is designed to work with local Ollama models (no API keys needed)
- Response parsing uses `BeanOutputConverter` to map AI output to Java records

### File Processing

- Only `.md` (markdown) files are accepted
- Files are read as UTF-8 encoded strings
- Multiple files can be processed in a single request
- Each file generates one `QuizResponse`

### MongoDB Schema

**Users Collection** (`users`):
- `_id`: MongoDB ObjectId (String)
- `username`: Unique indexed string
- `email`: Unique indexed string
- `password`: BCrypt hashed string
- `roles`: Array of embedded role documents
  - Each role: `{ id: Long, role: String }`

MongoDB indexes ensure username and email uniqueness at the database level.

### Mapper Pattern

The application uses a dual-mapper pattern to maintain layer separation:
- **Application Mappers** (`user.application.mapper.UserMapper`): Convert DTOs ↔ Domain Models, apply business logic (e.g., password encryption)
- **Infrastructure Mappers** (`user.infrastructure.output.database.mapper.UserEntityMapper`): Convert Database Entities ↔ Domain Models, pure data transformation

This ensures domain models remain independent of both API contracts (DTOs) and database implementation (Entities).

### Future Development

The codebase contains commented-out code suggesting planned features:
- MongoDB persistence of quiz results (QuizRepository)
- User association with quizzes (userId tracking)
- QuizDocument entity for database storage

### Maven Wrapper

The project includes Maven wrapper (`mvnw`/`mvnw.cmd`) so Maven installation is not required. Always use `./mvnw` instead of `mvn`.