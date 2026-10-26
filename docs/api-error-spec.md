# CareerHub API Error Specification

## 1. Overview

CareerHub uses a unified API response structure for both successful and failed requests.

The goal is to provide clear and consistent responses to API clients while avoiding exposure of internal implementation details or server stack traces.

---

## 2. Unified API Response

All APIs use the following basic response structure:

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

### Fields

| Field | Description |
|---|---|
| `code` | Response status code |
| `message` | Human-readable response description |
| `data` | Business data or detailed error information |

Successful and failed requests use the same outer response structure.

---

## 3. Successful Response

Example:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "Tom",
    "email": "tom@example.com",
    "status": "ACTIVE"
  }
}
```

The response is wrapped by:

```text
ApiResponse<T>
```

Different APIs can use different data types.

Examples:

```text
ApiResponse<UserResponse>
ApiResponse<List<UserResponse>>
ApiResponse<IPage<UserResponse>>
ApiResponse<Boolean>
```

---

## 4. Validation Errors

### 4.1 Request DTO Validation

Request DTO validation is used for JSON request bodies.

Typical example:

```text
@Valid
@RequestBody CreateUserRequest request
```

Validation rules are defined inside the DTO.

Example:

```java
@NotBlank(message = "username cannot be blank")
@Size(
        min = 2,
        max = 50,
        message = "username length must be between 2 and 50"
)
private String username;

@NotBlank(message = "email cannot be blank")
@Email(message = "email format is invalid")
private String email;
```

When DTO field validation fails, Spring throws:

```text
MethodArgumentNotValidException
```

Processing flow:

```text
HTTP JSON
    ↓
@RequestBody
    ↓
CreateUserRequest
    ↓
@Valid
    ↓
@NotBlank / @Size / @Email
    ↓
Validation Failed
    ↓
MethodArgumentNotValidException
    ↓
GlobalExceptionHandler
    ↓
HTTP 400
```

Example response:

```json
{
  "code": 400,
  "message": "validation failed",
  "data": [
    "username cannot be blank",
    "username length must be between 2 and 50",
    "email format is invalid"
  ]
}
```

The exception handler obtains validation information through:

```text
getBindingResult()
    ↓
getFieldErrors()
    ↓
FieldError
    ↓
getDefaultMessage()
```

---

## 5. Method Parameter Validation

Controller method parameters can also be validated directly.

Typical examples include:

```text
@Positive
@PathVariable Long id
```

and:

```text
@Min(1)
@RequestParam long pageNum
```

or:

```text
@Min(1)
@Max(100)
@RequestParam long pageSize
```

Examples of current rules:

```text
id > 0

pageNum >= 1

1 <= pageSize <= 100
```

When this type of validation fails, Spring throws:

```text
HandlerMethodValidationException
```

Example:

```http
GET /users/-1
```

The value `-1` can be converted to `Long`, but it violates:

```text
@Positive
```

Processing flow:

```text
/users/-1
    ↓
"-1" converted to Long
    ↓
id = -1
    ↓
@Positive validation fails
    ↓
HandlerMethodValidationException
    ↓
GlobalExceptionHandler
    ↓
HTTP 400
```

Example response:

```json
{
  "code": 400,
  "message": "validation failed",
  "data": [
    "id must be greater than 0"
  ]
}
```

---

## 6. Parameter Type Mismatch

A parameter may fail before validation if its value cannot be converted to the required Java type.

Example:

```http
GET /users/abc
```

The Controller expects:

```text
@PathVariable Long id
```

Spring first attempts:

```text
"abc"
↓
Long
```

The conversion fails.

Therefore `@Positive` is never reached.

Spring throws:

```text
MethodArgumentTypeMismatchException
```

Processing flow:

```text
/users/abc
    ↓
Read path variable
    ↓
Convert "abc" to Long
    ↓
Conversion Failed
    ↓
MethodArgumentTypeMismatchException
    ↓
GlobalExceptionHandler
    ↓
HTTP 400
```

Example response:

```json
{
  "code": 400,
  "message": "invalid parameter type",
  "data": null
}
```

---

## 7. Business Exceptions

Validation errors mean that the request itself is invalid.

Business exceptions are different:

> The request parameters are valid, but the requested business operation cannot be completed.

### 7.1 User Not Found

Example:

```http
GET /users/999999
```

`999999`:

```text
can be converted to Long
        ↓
is greater than 0
        ↓
passes parameter validation
```

The request therefore enters the normal business flow:

```text
Controller
    ↓
Service
    ↓
Mapper
    ↓
MySQL
```

If the user does not exist, the application throws:

```text
UserNotFoundException
```

Processing flow:

```text
GET /users/999999
    ↓
Parameter validation passes
    ↓
Controller
    ↓
Service
    ↓
Mapper
    ↓
MySQL
    ↓
User not found
    ↓
UserNotFoundException
    ↓
GlobalExceptionHandler
    ↓
HTTP 404
```

Example response:

```json
{
  "code": 404,
  "message": "User not found:999999",
  "data": null
}
```

---

## 8. Current Exception Mapping

| Exception | Scenario | HTTP Status |
|---|---|---:|
| `MethodArgumentNotValidException` | Request DTO field validation failure | 400 |
| `HandlerMethodValidationException` | Controller method parameter validation failure | 400 |
| `MethodArgumentTypeMismatchException` | Request parameter type conversion failure | 400 |
| `UserNotFoundException` | Requested business resource does not exist | 404 |

---

## 9. HTTP Status Rules

### 200 OK

The request was successfully processed.

Example:

```text
GET /users/1
```

---

### 400 Bad Request

The request itself is invalid.

Typical situations:

```text
username is blank

email format is invalid

id <= 0

pageNum < 1

pageSize < 1

pageSize > 100

/users/abc
```

---

### 404 Not Found

The request itself is valid, but the requested resource does not exist.

Example:

```text
GET /users/999999
```

---

## 10. 400 vs 404

Example:

```text
/users/abc
```

Result:

```text
Type cannot be converted
→ 400 Bad Request
```

Example:

```text
/users/-1
```

Result:

```text
Type is correct
but value violates validation rule
→ 400 Bad Request
```

Example:

```text
/users/999999
```

Result:

```text
Type is correct
Validation passes
User does not exist
→ 404 Not Found
```

---

## 11. Global Exception Handling

CareerHub uses:

```text
@RestControllerAdvice
```

to provide centralized exception handling.

Different exceptions are routed to different methods through:

```text
@ExceptionHandler
```

Conceptually:

```text
Controller / Validation
          ↓
       Exception
          ↓
GlobalExceptionHandler
          ↓
Find matching @ExceptionHandler
          ↓
Build ApiResponse
          ↓
Return ResponseEntity
          ↓
HTTP Response
```

This avoids writing repetitive `try-catch` blocks inside every Controller.

---

## 12. Complete Request Flow

### Successful Request

```text
HTTP Request
    ↓
Spring Parameter Binding
    ↓
Validation
    ↓
Validation Passed
    ↓
Controller
    ↓
Service
    ↓
Mapper
    ↓
MyBatis-Plus
    ↓
MySQL
    ↓
Entity
    ↓
Response DTO
    ↓
ApiResponse
    ↓
HTTP 200
```

### Failed Request

```text
HTTP Request
    ↓
Spring Parameter Binding / Validation
    ↓
Error Detected
    ↓
Exception
    ↓
GlobalExceptionHandler
    ↓
ApiResponse
    ↓
HTTP 4xx
```

### Business Failure

```text
HTTP Request
    ↓
Validation Passed
    ↓
Controller
    ↓
Service
    ↓
Business Error
    ↓
Custom Exception
    ↓
GlobalExceptionHandler
    ↓
ApiResponse
    ↓
HTTP 4xx
```

---

## 13. Validation Responsibility

Different validation mechanisms have different responsibilities.

```text
@Valid
→ triggers validation of fields inside a DTO
```

```text
@NotBlank
@Email
@Size
@Positive
@Min
@Max
→ define specific validation rules
```

Examples:

```text
@RequestBody DTO
    ↓
@Valid
    ↓
@NotBlank / @Email / @Size
```

and:

```text
@PathVariable id
    ↓
@Positive
```

and:

```text
@RequestParam pageNum / pageSize
    ↓
@Min / @Max
```

---

## 14. Security Principle

API responses should contain enough information for clients to understand and correct invalid requests.

However, API responses should not expose:

```text
server stack traces
internal class paths
database implementation details
sensitive configuration
internal exception details that are not intended for clients
```

Internal exceptions should be translated into clear client-facing responses through `GlobalExceptionHandler`.

---

## 15. Current Design Principle

CareerHub currently follows this principle:

```text
Internal exception types may differ
        ↓
GlobalExceptionHandler handles them centrally
        ↓
External API response format remains consistent
```

The current unified response structure is:

```json
{
  "code": 400,
  "message": "validation failed",
  "data": []
}
```

or:

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

This specification will continue to evolve as authentication, Resume, Job, Redis and other modules are added.