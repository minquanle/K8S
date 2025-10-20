# REST API + JWT Roles (USER/ADMIN)

This module exposes CRUD REST APIs under `/api/items` and protects them with JWT.

- USER role: can only GET
- ADMIN role: can GET, POST, PUT, DELETE

Seed users (created at startup):
- admin / admin123 (roles: ADMIN, USER)
- user / user123 (role: USER)

## Build

```bat
cd C:\Users\lmqua\OneDrive\Desktop\gs-securing-web-main\initial
mvn clean package
```

## Run

```bat
cd C:\Users\lmqua\OneDrive\Desktop\gs-securing-web-main\initial
mvn spring-boot:run
```

H2 console: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:demo`
- User: `sa`
- Password: (empty)

## Obtain JWT token

Use Windows cmd.exe `curl.exe`:

```bat
curl.exe -s -X POST http://localhost:8080/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"admin\",\"password\":\"admin123\"}"
```

Copy the `token` field from the JSON response.

Set an environment variable with the token:

```bat
set TOKEN=PASTE_JWT_HERE
```

## Test APIs

- USER can GET only (use token from user/user123)
```bat
curl.exe -s http://localhost:8080/api/items -H "Authorization: Bearer %TOKEN%"
```

- ADMIN can create/update/delete
```bat
:: Create
curl.exe -s -X POST http://localhost:8080/api/items ^
  -H "Authorization: Bearer %TOKEN%" ^
  -H "Content-Type: application/json" ^
  -d "{\"name\":\"Book\",\"description\":\"Spring Boot\"}"

:: Update
curl.exe -s -X PUT http://localhost:8080/api/items/1 ^
  -H "Authorization: Bearer %TOKEN%" ^
  -H "Content-Type: application/json" ^
  -d "{\"name\":\"Book v2\",\"description\":\"Updated\"}"

:: Delete
curl.exe -s -X DELETE http://localhost:8080/api/items/1 ^
  -H "Authorization: Bearer %TOKEN%"
```

## Notes
- JWT config in `src/main/resources/application.properties` (`app.jwt.secret`, `app.jwt.expiration-ms`). Replace the secret for production use.
- API is stateless (no sessions). Web pages (/, /login, /admin) still use form login.
- If you previously saw "No goals have been specified", you ran `mvn` without a goal. Use `mvn clean package` or `mvn spring-boot:run`.

