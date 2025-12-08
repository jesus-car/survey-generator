# Plan de Implementación: Historial de Quizzes con Persistencia

## Fecha: 2025-12-07

---

## 1. Resumen Ejecutivo

Implementar funcionalidad para que usuarios autenticados puedan:
- Subir archivos `.md` que generan quizzes mediante IA
- Almacenar quizzes generados en MongoDB asociados al usuario
- Ver historial de quizzes previos (solo statement/título)
- Visualizar quiz completo al hacer click

---

## 2. Estado Actual del Proyecto

### ✅ Lo que YA existe:

#### Módulo User (Completo)
- **Autenticación JWT**: Login y registro funcionando
- **UserModel**: Dominio con id, username, email, password, roles
- **UserEntity**: Entidad MongoDB en colección "users"
- **UserRepository**: MongoRepository con queries personalizadas
- **JwtAuthorizationFilter**: Extrae y valida JWT, establece SecurityContext
- **Arquitectura hexagonal**: Capas bien separadas (domain, application, infrastructure)

#### Módulo Document (Parcial)
- **DocumentController**: Endpoint `POST /api/v1/documents/upload`
  - Acepta múltiples archivos `.md`
  - Valida archivos con `FileValidator`
  - Retorna `List<QuizResponse>`
- **DocumentService**: Orquesta generación de quizzes
  - Extrae contenido de archivos
  - Llama a IA para generar preguntas
  - **NO persiste en base de datos actualmente**
- **QuizResponse DTO**: Estructura completa
  ```java
  QuizResponse {
      String statement;  // Título del quiz
      List<Question> questions;

      Question {
          String question;
          QuestionOptions options {
              List<String> options;
              String answer;
          }
      }
  }
  ```
- **AiProcessorAdapter**: Integración con Ollama para generar quizzes
- **Código comentado**: Sugiere funcionalidad planeada (líneas 20, 31-44, 58-66 en DocumentService)

#### Infraestructura
- **MongoDB**: Configurado y funcionando
- **Spring Security**: Filtros JWT operativos
- **Spring AI**: Integración con Ollama (modelo gemma3)

### ❌ Lo que FALTA implementar:

1. **Capa de Dominio para Quiz**
   - QuizModel (domain model)
   - QuestionModel (domain model anidado)

2. **Capa de Persistencia**
   - QuizEntity y QuestionEntity (MongoDB documents)
   - QuizRepository (MongoRepository interface)

3. **Arquitectura Hexagonal para Document**
   - Port interface: `QuizAccessDatabasePort`
   - Adapter: `QuizAccessDatabaseAdapter`
   - Mappers: `QuizMapper`, `QuizEntityMapper`

4. **Servicios**
   - Modificar `DocumentService` para guardar quizzes
   - Crear `QuizService` para operaciones de consulta

5. **Endpoints**
   - Modificar `POST /upload` para autenticar y asociar userId
   - Crear `GET /api/v1/quizzes/history` (retornar solo id + statement + createdAt)
   - Crear `GET /api/v1/quizzes/{id}` (retornar quiz completo)

6. **Seguridad**
   - Proteger endpoint `/upload` con JWT
   - Extraer userId del token JWT en el controller

---

## 3. Plan de Acción Detallado

### **FASE 1: Crear Capa de Dominio (Domain Layer)**

#### 1.1 QuizModel (Domain Model)
**Archivo**: `src/main/java/com/surveygenerator/surveygenerator/document/domain/model/QuizModel.java`

```java
package com.surveygenerator.surveygenerator.document.domain.model;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class QuizModel {
    private String id;
    private String userId;  // Referencia al usuario propietario
    private String statement;  // Título/enunciado del quiz
    private List<QuestionModel> questions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

#### 1.2 QuestionModel (Domain Model)
**Archivo**: `src/main/java/com/surveygenerator/surveygenerator/document/domain/model/QuestionModel.java`

```java
package com.surveygenerator.surveygenerator.document.domain.model;

import lombok.*;
import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class QuestionModel {
    private String question;
    private List<String> options;
    private String answer;
}
```

---

### **FASE 2: Crear Capa de Infraestructura - Database (Output Adapter)**

#### 2.1 QuizEntity (MongoDB Document)
**Archivo**: `src/main/java/com/surveygenerator/surveygenerator/document/infrastructure/output/database/entity/QuizEntity.java`

```java
package com.surveygenerator.surveygenerator.document.infrastructure.output.database.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "quizzes")
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class QuizEntity {
    @Id
    private String id;

    @Indexed  // Índice para búsquedas por usuario
    private String userId;

    private String statement;
    private List<QuestionEntity> questions;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

#### 2.2 QuestionEntity (Embedded Document)
**Archivo**: `src/main/java/com/surveygenerator/surveygenerator/document/infrastructure/output/database/entity/QuestionEntity.java`

```java
package com.surveygenerator.surveygenerator.document.infrastructure.output.database.entity;

import lombok.*;
import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class QuestionEntity {
    private String question;
    private List<String> options;
    private String answer;
}
```

#### 2.3 QuizRepository (MongoRepository)
**Archivo**: `src/main/java/com/surveygenerator/surveygenerator/document/infrastructure/output/database/repository/QuizRepository.java`

```java
package com.surveygenerator.surveygenerator.document.infrastructure.output.database.repository;

import com.surveygenerator.surveygenerator.document.infrastructure.output.database.entity.QuizEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface QuizRepository extends MongoRepository<QuizEntity, String> {

    // Buscar todos los quizzes de un usuario, ordenados por fecha (más recientes primero)
    List<QuizEntity> findByUserIdOrderByCreatedAtDesc(String userId);

    // Buscar un quiz específico que pertenezca a un usuario (para seguridad)
    Optional<QuizEntity> findByIdAndUserId(String id, String userId);
}
```

#### 2.4 QuizEntityMapper
**Archivo**: `src/main/java/com/surveygenerator/surveygenerator/document/infrastructure/output/database/mapper/QuizEntityMapper.java`

```java
package com.surveygenerator.surveygenerator.document.infrastructure.output.database.mapper;

import com.surveygenerator.surveygenerator.document.domain.model.QuestionModel;
import com.surveygenerator.surveygenerator.document.domain.model.QuizModel;
import com.surveygenerator.surveygenerator.document.infrastructure.output.database.entity.QuestionEntity;
import com.surveygenerator.surveygenerator.document.infrastructure.output.database.entity.QuizEntity;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class QuizEntityMapper {

    public QuizModel toModel(QuizEntity entity) {
        return QuizModel.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .statement(entity.getStatement())
                .questions(entity.getQuestions().stream()
                        .map(this::toQuestionModel)
                        .toList())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public QuizEntity toEntity(QuizModel model) {
        return QuizEntity.builder()
                .id(model.getId())
                .userId(model.getUserId())
                .statement(model.getStatement())
                .questions(model.getQuestions().stream()
                        .map(this::toQuestionEntity)
                        .toList())
                .createdAt(model.getCreatedAt() != null ? model.getCreatedAt() : LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private QuestionModel toQuestionModel(QuestionEntity entity) {
        return QuestionModel.builder()
                .question(entity.getQuestion())
                .options(entity.getOptions())
                .answer(entity.getAnswer())
                .build();
    }

    private QuestionEntity toQuestionEntity(QuestionModel model) {
        return QuestionEntity.builder()
                .question(model.getQuestion())
                .options(model.getOptions())
                .answer(model.getAnswer())
                .build();
    }
}
```

---

### **FASE 3: Crear Port Interface (Application Layer)**

#### 3.1 QuizAccessDatabasePort
**Archivo**: `src/main/java/com/surveygenerator/surveygenerator/document/application/port/output/QuizAccessDatabasePort.java`

```java
package com.surveygenerator.surveygenerator.document.application.port.output;

import com.surveygenerator.surveygenerator.document.domain.model.QuizModel;
import java.util.List;
import java.util.Optional;

public interface QuizAccessDatabasePort {

    QuizModel save(QuizModel quizModel);

    List<QuizModel> findAllByUserId(String userId);

    Optional<QuizModel> findByIdAndUserId(String id, String userId);
}
```

---

### **FASE 4: Implementar Adapter (Output)**

#### 4.1 QuizAccessDatabaseAdapter
**Archivo**: `src/main/java/com/surveygenerator/surveygenerator/document/infrastructure/output/database/adapter/QuizAccessDatabaseAdapter.java`

```java
package com.surveygenerator.surveygenerator.document.infrastructure.output.database.adapter;

import com.surveygenerator.surveygenerator.document.application.port.output.QuizAccessDatabasePort;
import com.surveygenerator.surveygenerator.document.domain.model.QuizModel;
import com.surveygenerator.surveygenerator.document.infrastructure.output.database.mapper.QuizEntityMapper;
import com.surveygenerator.surveygenerator.document.infrastructure.output.database.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class QuizAccessDatabaseAdapter implements QuizAccessDatabasePort {

    private final QuizRepository quizRepository;
    private final QuizEntityMapper quizEntityMapper;

    @Override
    public QuizModel save(QuizModel quizModel) {
        var entity = quizEntityMapper.toEntity(quizModel);
        var savedEntity = quizRepository.save(entity);
        return quizEntityMapper.toModel(savedEntity);
    }

    @Override
    public List<QuizModel> findAllByUserId(String userId) {
        return quizRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(quizEntityMapper::toModel)
                .toList();
    }

    @Override
    public Optional<QuizModel> findByIdAndUserId(String id, String userId) {
        return quizRepository.findByIdAndUserId(id, userId)
                .map(quizEntityMapper::toModel);
    }
}
```

---

### **FASE 5: Crear Mappers (Application Layer)**

#### 5.1 QuizMapper (DTO ↔ Domain Model)
**Archivo**: `src/main/java/com/surveygenerator/surveygenerator/document/application/mapper/QuizMapper.java`

```java
package com.surveygenerator.surveygenerator.document.application.mapper;

import com.surveygenerator.surveygenerator.document.application.dto.response.QuizResponse;
import com.surveygenerator.surveygenerator.document.domain.model.QuestionModel;
import com.surveygenerator.surveygenerator.document.domain.model.QuizModel;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class QuizMapper {

    // Convierte QuizResponse (de la IA) a QuizModel para persistir
    public QuizModel toModel(QuizResponse response, String userId) {
        return QuizModel.builder()
                .userId(userId)
                .statement(response.statement())
                .questions(response.questions().stream()
                        .map(this::toQuestionModel)
                        .toList())
                .createdAt(LocalDateTime.now())
                .build();
    }

    // Convierte QuizModel (de DB) a QuizResponse para enviar al frontend
    public QuizResponse toResponse(QuizModel model) {
        return QuizResponse.builder()
                .statement(model.getStatement())
                .questions(model.getQuestions().stream()
                        .map(this::toQuestionResponse)
                        .toList())
                .build();
    }

    private QuestionModel toQuestionModel(QuizResponse.Question question) {
        return QuestionModel.builder()
                .question(question.question())
                .options(question.options().options())
                .answer(question.options().answer())
                .build();
    }

    private QuizResponse.Question toQuestionResponse(QuestionModel model) {
        return new QuizResponse.Question(
                model.getQuestion(),
                new QuizResponse.Question.QuestionOptions(
                        model.getOptions(),
                        model.getAnswer()
                )
        );
    }
}
```

---

### **FASE 6: Crear DTOs para Historial**

#### 6.1 QuizHistoryResponse (Solo id + statement + fecha)
**Archivo**: `src/main/java/com/surveygenerator/surveygenerator/document/application/dto/response/QuizHistoryResponse.java`

```java
package com.surveygenerator.surveygenerator.document.application.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record QuizHistoryResponse(
        @JsonProperty("id") String id,
        @JsonProperty("statement") String statement,
        @JsonProperty("createdAt") LocalDateTime createdAt
) {}
```

---

### **FASE 7: Crear QuizService**

#### 7.1 QuizService
**Archivo**: `src/main/java/com/surveygenerator/surveygenerator/document/application/service/QuizService.java`

```java
package com.surveygenerator.surveygenerator.document.application.service;

import com.surveygenerator.surveygenerator.document.application.dto.response.QuizHistoryResponse;
import com.surveygenerator.surveygenerator.document.application.dto.response.QuizResponse;
import com.surveygenerator.surveygenerator.document.application.mapper.QuizMapper;
import com.surveygenerator.surveygenerator.document.application.port.output.QuizAccessDatabasePort;
import com.surveygenerator.surveygenerator.document.domain.model.QuizModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizAccessDatabasePort quizAccessDatabasePort;
    private final QuizMapper quizMapper;

    public List<QuizHistoryResponse> getUserQuizHistory(String userId) {
        return quizAccessDatabasePort.findAllByUserId(userId)
                .stream()
                .map(this::toHistoryResponse)
                .toList();
    }

    public QuizResponse getQuizById(String quizId, String userId) {
        QuizModel quizModel = quizAccessDatabasePort.findByIdAndUserId(quizId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Quiz no encontrado o no pertenece al usuario"));

        return quizMapper.toResponse(quizModel);
    }

    private QuizHistoryResponse toHistoryResponse(QuizModel model) {
        return QuizHistoryResponse.builder()
                .id(model.getId())
                .statement(model.getStatement())
                .createdAt(model.getCreatedAt())
                .build();
    }
}
```

---

### **FASE 8: Modificar DocumentService**

#### 8.1 Actualizar DocumentService para persistir quizzes
**Archivo**: `src/main/java/com/surveygenerator/surveygenerator/document/application/service/DocumentService.java`

**MODIFICAR** el método `generateQuestions` para:
1. Aceptar `userId` como parámetro
2. Guardar cada quiz generado en la base de datos
3. Retornar los quizzes

```java
// ANTES:
public List<QuizResponse> generateQuestions(List<MultipartFile> files) {
    return files.stream()
            .map(fileContentProcessor::extractContent)
            .map(this::validateContent)
            .map(aiProcessorPort::generateQuestions)
            .toList();
}

// DESPUÉS:
private final QuizAccessDatabasePort quizAccessDatabasePort;
private final QuizMapper quizMapper;

public List<QuizResponse> generateQuestions(List<MultipartFile> files, String userId) {
    return files.stream()
            .map(fileContentProcessor::extractContent)
            .map(this::validateContent)
            .map(aiProcessorPort::generateQuestions)
            .map(response -> saveQuiz(response, userId))
            .toList();
}

private QuizResponse saveQuiz(QuizResponse response, String userId) {
    QuizModel quizModel = quizMapper.toModel(response, userId);
    quizAccessDatabasePort.save(quizModel);
    return response;
}
```

---

### **FASE 9: Crear QuizController**

#### 9.1 QuizController para historial y detalle
**Archivo**: `src/main/java/com/surveygenerator/surveygenerator/document/infrastructure/input/controller/QuizController.java`

```java
package com.surveygenerator.surveygenerator.document.infrastructure.input.controller;

import com.surveygenerator.surveygenerator.document.application.dto.response.QuizHistoryResponse;
import com.surveygenerator.surveygenerator.document.application.dto.response.QuizResponse;
import com.surveygenerator.surveygenerator.document.application.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    // Obtener historial de quizzes del usuario (solo id, statement, fecha)
    @GetMapping("/history")
    public ResponseEntity<List<QuizHistoryResponse>> getUserQuizHistory(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        // El username está en el JWT, necesitamos obtener el userId de la DB
        // Opción 1: Agregar userId al token JWT
        // Opción 2: Hacer lookup en UserService
        // Por ahora, usaremos username directamente (ajustar según tu implementación)

        String userId = getUserIdFromUserDetails(userDetails);
        return ResponseEntity.ok(quizService.getUserQuizHistory(userId));
    }

    // Obtener quiz completo por ID
    @GetMapping("/{quizId}")
    public ResponseEntity<QuizResponse> getQuizById(
            @PathVariable String quizId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String userId = getUserIdFromUserDetails(userDetails);
        return ResponseEntity.ok(quizService.getQuizById(quizId, userId));
    }

    // Helper method - ajustar según tu implementación de UserDetails
    private String getUserIdFromUserDetails(UserDetails userDetails) {
        // Opción 1: Si UserDetails es una implementación custom que incluye userId
        // return ((CustomUserDetails) userDetails).getUserId();

        // Opción 2: Si solo tienes username, hacer lookup
        // return userService.findByUsername(userDetails.getUsername()).getId();

        // Temporal: retornar username (CAMBIAR en implementación real)
        return userDetails.getUsername();
    }
}
```

---

### **FASE 10: Modificar DocumentController**

#### 10.1 Actualizar DocumentController para obtener userId
**Archivo**: `src/main/java/com/surveygenerator/surveygenerator/document/infrastructure/input/controller/DocumentController.java`

**MODIFICAR** para:
1. Obtener usuario autenticado
2. Pasar userId a DocumentService

```java
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

@PostMapping("/upload")
public ResponseEntity<List<QuizResponse>> uploadDocument(
        @RequestParam("files") List<MultipartFile> files,
        @AuthenticationPrincipal UserDetails userDetails
) {
    ValidatorResult<List<MultipartFile>> validationResult = fileValidator.validateFiles(files);

    if (!validationResult.isValid()) {
        return ResponseEntity.badRequest().build();
    }

    // Obtener userId del usuario autenticado
    String userId = getUserIdFromUserDetails(userDetails);

    return ResponseEntity.ok(
            documentService.generateQuestions(validationResult.getData(), userId)
    );
}

private String getUserIdFromUserDetails(UserDetails userDetails) {
    // IMPLEMENTAR según tu UserDetails
    return userDetails.getUsername(); // Temporal
}
```

---

### **FASE 11: Configurar Seguridad**

#### 11.1 Actualizar AuthConfiguration para proteger endpoints
**Archivo**: `src/main/java/com/surveygenerator/surveygenerator/user/infrastructure/config/AuthConfiguration.java`

**MODIFICAR** la configuración de seguridad:

```java
// ANTES:
.anyRequest().permitAll()

// DESPUÉS:
.requestMatchers("/api/v1/users/register", "/api/v1/users/login").permitAll()
.anyRequest().authenticated()
```

---

### **FASE 12: Mejorar UserDetails para incluir userId**

#### 12.1 Crear CustomUserDetails
**Archivo**: `src/main/java/com/surveygenerator/surveygenerator/user/infrastructure/config/CustomUserDetails.java`

```java
package com.surveygenerator.surveygenerator.user.infrastructure.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;

@RequiredArgsConstructor
@Getter
public class CustomUserDetails implements UserDetails {

    private final String userId;  // ID de MongoDB
    private final String username;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean active;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return active;
    }

    @Override
    public boolean isAccountNonLocked() {
        return active;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return active;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
```

#### 12.2 Actualizar UserDetailsServiceConfig
**Archivo**: `src/main/java/com/surveygenerator/surveygenerator/user/infrastructure/config/UserDetailsServiceConfig.java`

```java
// MODIFICAR para retornar CustomUserDetails con userId incluido
@Override
public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    UserModel user = userAccessDatabasePort.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

    return new CustomUserDetails(
            user.getId(),  // ← Incluir userId
            user.getUsername(),
            user.getPassword(),
            user.getRoles().stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toSet()),
            user.isActive()
    );
}
```

#### 12.3 Actualizar JwtUtils para incluir userId en token
**Archivo**: Actualizar generación de JWT

```java
// En el método que genera el token, agregar claim:
.claim("userId", user.getId())
```

#### 12.4 Actualizar JwtAuthorizationFilter para establecer CustomUserDetails
**Modificar** para que el filtro use `CustomUserDetails` al establecer la autenticación.

---

## 4. Orden de Implementación Recomendado

### Paso 1: Domain Layer (30 min)
- [ ] QuizModel
- [ ] QuestionModel

### Paso 2: Infrastructure - Database Entities (30 min)
- [ ] QuizEntity
- [ ] QuestionEntity
- [ ] QuizRepository

### Paso 3: Infrastructure - Mappers (30 min)
- [ ] QuizEntityMapper

### Paso 4: Application - Port Interface (15 min)
- [ ] QuizAccessDatabasePort

### Paso 5: Infrastructure - Adapter (20 min)
- [ ] QuizAccessDatabaseAdapter

### Paso 6: Application - Mapper (30 min)
- [ ] QuizMapper

### Paso 7: Application - DTOs (15 min)
- [ ] QuizHistoryResponse

### Paso 8: Application - Services (45 min)
- [ ] QuizService
- [ ] Modificar DocumentService

### Paso 9: Mejorar UserDetails con userId (60 min)
- [ ] CustomUserDetails
- [ ] Actualizar UserDetailsServiceConfig
- [ ] Actualizar JwtUtils (agregar userId a token)
- [ ] Actualizar JwtAuthorizationFilter

### Paso 10: Infrastructure - Controllers (30 min)
- [ ] QuizController
- [ ] Modificar DocumentController

### Paso 11: Configuración de Seguridad (15 min)
- [ ] Actualizar AuthConfiguration

### Paso 12: Testing (1-2 horas)
- [ ] Probar registro y login
- [ ] Probar upload con JWT
- [ ] Probar persistencia de quizzes
- [ ] Probar endpoint de historial
- [ ] Probar endpoint de detalle

---

## 5. Consideraciones Técnicas

### 5.1 Gestión de userId
**IMPORTANTE**: Actualmente `UserDetails` de Spring Security solo contiene username. Para obtener el `userId` (MongoDB ID) hay dos opciones:

**Opción A (Recomendada)**: Agregar userId al JWT token
- Modificar `JwtUtils` para incluir claim `userId`
- Crear `CustomUserDetails` que incluya `userId`
- Extraer `userId` directamente del token en controllers

**Opción B**: Lookup adicional
- En cada request, hacer lookup en UserRepository usando username
- Menos eficiente, más queries a DB

### 5.2 Seguridad
- **Validación de ownership**: El endpoint `GET /quizzes/{id}` debe verificar que el quiz pertenezca al usuario autenticado
- **Ya implementado** en `QuizRepository.findByIdAndUserId()`

### 5.3 Índices de MongoDB
- Crear índice en `quizzes.userId` para optimizar búsquedas por usuario
- **Ya considerado** con `@Indexed` en `QuizEntity.userId`

### 5.4 Manejo de Errores
- Si usuario solicita quiz que no existe: `404 Not Found`
- Si usuario solicita quiz de otro usuario: `403 Forbidden` o `404 Not Found`
- Si upload falla: `400 Bad Request` con mensaje descriptivo

---

## 6. Estructura de MongoDB Final

### Colección: `users`
```json
{
  "_id": "507f1f77bcf86cd799439011",
  "username": "johndoe",
  "email": "john@example.com",
  "password": "$2a$10$...",
  "roles": [
    { "id": 1, "role": "USER" }
  ],
  "createdAt": "2025-12-07T10:00:00",
  "updatedAt": "2025-12-07T10:00:00",
  "active": true
}
```

### Colección: `quizzes` (NUEVA)
```json
{
  "_id": "507f1f77bcf86cd799439012",
  "userId": "507f1f77bcf86cd799439011",
  "statement": "Quiz sobre conceptos de Java",
  "questions": [
    {
      "question": "¿Qué es una variable?",
      "options": [
        "Un contenedor de datos",
        "Una función",
        "Una clase",
        "Un método",
        "Un paquete"
      ],
      "answer": "Un contenedor de datos"
    }
  ],
  "createdAt": "2025-12-07T11:30:00",
  "updatedAt": "2025-12-07T11:30:00"
}
```

---

## 7. Endpoints Finales

### Autenticación
- `POST /api/v1/users/register` - Registro (público)
- `POST /api/v1/users/login` - Login (público)

### Quizzes
- `POST /api/v1/documents/upload` - Subir .md y generar quiz (**autenticado**)
  - Headers: `Authorization: Bearer <token>`
  - Body: `multipart/form-data` con archivos .md
  - Response: `List<QuizResponse>` (quiz completo generado)

- `GET /api/v1/quizzes/history` - Obtener historial (**autenticado**)
  - Headers: `Authorization: Bearer <token>`
  - Response: `List<QuizHistoryResponse>` (solo id, statement, createdAt)

- `GET /api/v1/quizzes/{quizId}` - Obtener quiz completo (**autenticado**)
  - Headers: `Authorization: Bearer <token>`
  - Response: `QuizResponse` (quiz completo con preguntas)

---

## 8. Flujo de Usuario Final

1. **Usuario se registra**: `POST /users/register`
2. **Usuario inicia sesión**: `POST /users/login` → Recibe JWT
3. **Usuario sube archivo .md**: `POST /documents/upload` (con JWT)
   - Backend genera quiz con IA
   - Backend guarda quiz en MongoDB asociado al userId
   - Backend retorna quiz generado al frontend
4. **Usuario ve historial**: `GET /quizzes/history` (con JWT)
   - Frontend muestra lista en menú lateral: solo `statement` + `createdAt`
5. **Usuario hace click en quiz**: `GET /quizzes/{id}` (con JWT)
   - Frontend muestra quiz completo con preguntas y alternativas

---

## 9. Testing Checklist

### Tests Unitarios
- [ ] QuizMapper: conversión DTO ↔ Model
- [ ] QuizEntityMapper: conversión Entity ↔ Model
- [ ] QuizService: lógica de negocio
- [ ] DocumentService: persistencia de quizzes

### Tests de Integración
- [ ] QuizRepository: queries a MongoDB
- [ ] QuizController: endpoints REST
- [ ] DocumentController: upload con autenticación

### Tests E2E
- [ ] Flujo completo: registro → login → upload → historial → detalle

---

## 10. Mejoras Futuras (Post-MVP)

- [ ] Paginación en endpoint de historial
- [ ] Búsqueda de quizzes por statement
- [ ] Eliminación de quizzes
- [ ] Edición de quizzes guardados
- [ ] Compartir quizzes entre usuarios
- [ ] Estadísticas de uso (quizzes generados, preguntas totales)
- [ ] Caché de quizzes frecuentemente consultados (Redis)
- [ ] Exportar quiz a PDF

---

## Resumen de Archivos a Crear/Modificar

### CREAR (15 archivos nuevos)
1. `document/domain/model/QuizModel.java`
2. `document/domain/model/QuestionModel.java`
3. `document/infrastructure/output/database/entity/QuizEntity.java`
4. `document/infrastructure/output/database/entity/QuestionEntity.java`
5. `document/infrastructure/output/database/repository/QuizRepository.java`
6. `document/infrastructure/output/database/mapper/QuizEntityMapper.java`
7. `document/infrastructure/output/database/adapter/QuizAccessDatabaseAdapter.java`
8. `document/application/port/output/QuizAccessDatabasePort.java`
9. `document/application/mapper/QuizMapper.java`
10. `document/application/dto/response/QuizHistoryResponse.java`
11. `document/application/service/QuizService.java`
12. `document/infrastructure/input/controller/QuizController.java`
13. `user/infrastructure/config/CustomUserDetails.java`

### MODIFICAR (5 archivos existentes)
1. `document/application/service/DocumentService.java`
2. `document/infrastructure/input/controller/DocumentController.java`
3. `user/infrastructure/config/AuthConfiguration.java`
4. `user/infrastructure/config/UserDetailsServiceConfig.java`
5. `user/infrastructure/config/JwtUtils.java` (si existe, o el archivo que genera tokens)
6. `user/infrastructure/config/JwtAuthorizationFilter.java`

---

## Estimación Total

**Tiempo estimado**: 6-8 horas de desarrollo
- Dominio + Entidades: 1 hora
- Repositorios + Adapters: 1.5 horas
- Servicios + Mappers: 1.5 horas
- Controllers + Seguridad: 1.5 horas
- CustomUserDetails + JWT: 1.5 horas
- Testing: 2 horas

**Complejidad**: Media
**Prioridad**: Alta

---

**Última actualización**: 2025-12-07