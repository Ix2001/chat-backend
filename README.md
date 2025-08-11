# Chat Backend — README

> Бэкенд корпоративного чата с авторизацией через Keycloak, реальным временем на WebSocket, файлами через S3/MinIO (presigned + multipart) и сигналингом WebRTC для аудио/видеозвонков.
> Java 21 • Spring Boot 3.1.x • PostgreSQL • Redis • MapStruct • Lombok

---

## Содержание

* [Архитектура](#архитектура)
* [Требования](#требования)
* [Быстрый старт (IntelliJ IDEA)](#быстрый-старт-intellij-idea)
* [Docker Compose для инфраструктуры](#docker-compose-для-инфраструктуры)
* [Конфигурация приложения](#конфигурация-приложения)
* [Модель данных](#модель-данных)
* [Безопасность и Keycloak](#безопасность-и-keycloak)
* [REST API (шпаргалка)](#rest-api-шпаргалка)
* [WebSocket (протокол и тесты)](#websocket-протокол-и-тесты)
* [Файлы: S3/MinIO, presigned + multipart](#файлы-s3minio-presigned--multipart)
* [Звонки: WebRTC сигналинг](#звонки-webrtc-сигналинг)
* [Асинхронность и масштабирование](#асинхронность-и-масштабирование)
* [Производительность](#производительность)
* [Отладка и типовые ошибки](#отладка-и-типовые-ошибки)
* [FAQ](#faq)

---

## Архитектура

* **Слои**:
  `controller` (REST) • `websocket` (WS handler) • `service` (бизнес-логика) • `mapper` (MapStruct) • `repository` (JPA) • `model` (Entity) • `dto` (наружные модели)

* **Хранилища**:
  PostgreSQL — основные данные
  Redis — кэш + Pub/Sub (реальное время и горизонтальное масштабирование WebSocket)

* **Реальное время**:
  WebSocket `/ws/chat` с аутентификацией по JWT (из Keycloak) через Handshake Interceptor.

* **Файлы**:
  S3/MinIO + presigned URLs (сервер **не** проксирует большие байты). Multipart upload.

* **Шифрование сообщений**:
  **End-to-end на клиенте.** Бэкенд хранит публичные ключи, но контент — шифротекст.

* **Аудио/видео**:
  Сигналинг WebRTC по WS (SDP/ICE). Медиа — P2P/через TURN (вне бэкенда).

---

## Требования

* JDK 21 (например, Amazon Corretto 21)
* Maven 3.9+
* PostgreSQL 15+
* Redis 7+
* S3-совместимое хранилище (MinIO локально или AWS S3)
* Keycloak 21+ (внешний IdP)
* IntelliJ IDEA 2024+ (реком.)

---

## Быстрый старт (IntelliJ IDEA)

1. Подними инфраструктуру (Postgres, Redis, MinIO) через docker-compose (ниже).
2. Создай БД `chat` и пользователя/пароль `chat:chat` (compose уже создаёт).
3. В `src/main/resources/application.yml` укажи:

    * `spring.security.oauth2.resourceserver.jwt.issuer-uri` = **ровно как в `iss` токена Keycloak**
      пример: `https://auth.dev.hr.alabuga.space/realms/Alabuga`
    * S3/MinIO параметры (endpoint, access/secret).
4. В IDEA: `Maven -> Reimport` → запусти `ChatBackendApplication`.
5. Получи токен в Keycloak, протестируй REST/WS (см. ниже).

---

## Docker Compose для инфраструктуры

```yaml
version: "3.8"
services:
  postgres:
    image: postgres:15
    environment:
      POSTGRES_DB: chat
      POSTGRES_USER: chat
      POSTGRES_PASSWORD: chat
    ports: ["5432:5432"]

  redis:
    image: redis:7
    ports: ["6379:6379"]

  minio:
    image: minio/minio:latest
    command: server /data --console-address ":9001"
    environment:
      MINIO_ROOT_USER: minioadmin
      MINIO_ROOT_PASSWORD: minioadmin
    ports: ["9000:9000", "9001:9001"]
    volumes:
      - minio-data:/data
volumes:
  minio-data:
```

После старта MinIO: зайди на `http://localhost:9001` → создай **приватный** бакет `chat-files`.

---

## Конфигурация приложения

`src/main/resources/application.yml` (пример):

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/chat
    username: chat
    password: chat
  jpa:
    hibernate:
      ddl-auto: validate      # для первого раза можно update
    properties:
      hibernate.jdbc.time_zone: UTC

  data:
    redis:
      host: localhost
      port: 6379

  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://auth.dev.hr.alabuga.space/realms/Alabuga

app:
  files:
    bucket: chat-files
    presign:
      part-duration: PT15M       # срок жизни presigned для части
      download-duration: PT1H    # срок жизни presigned на скачивание
    multipart:
      part-size: 8388608         # 8 MiB

s3:
  endpoint: http://localhost:9000
  region: us-east-1
  access-key: minioadmin
  secret-key: minioadmin
  path-style-access: true

logging:
  level:
    com.company.chat: INFO
```

> **Важно:** `issuer-uri` должен **буквально совпадать** с `iss` в токене.

---

## Модель данных

* **User**: `id`, `username`, `displayName`, `publicKey?`, audit
* **Room**: `id`, `type: DIRECT|GROUP`, `name?`, audit
* **Membership**: `room_id`, `user_id`, `role: OWNER|MEMBER`, audit
* **Message**: `id`, `room(FK)`, `sender(FK)`, `type: TEXT|ENCRYPTED|FILE|SYSTEM`, `content`, `timestamp`
* **FileMetadata**: `id`, `room_id`, `owner_id`, `bucket`, `object_key`, `filename_original`, `size`, `content_type`, `status: READY|QUARANTINE|DELETED`, audit
* **UploadSession**: `id(UUID)`, `room_id`, `owner_id`, `upload_id(S3)`, `object_key`, `part_size`, `state`, `expires_at`

---

## Безопасность и Keycloak

* Приложение — **Resource Server** (Spring Security).
  Оно **не использует** `client_id/client_secret`. Задача — **валидировать JWT**, пришедший в `Authorization: Bearer <access_token>`.

* Отправитель **не передаётся** в API: сервер берёт его **из токена**.
  В REST/WS:

    * Парсинг токена → `Jwt` → `sub`, `preferred_username`, `name…`
    * `UserService.ensureFromToken(...)` создаёт/апдейтит локального пользователя.
    * Везде используем его `userId`.

* **WebSocket аутентификация**:
  В Handshake Interceptor читаем токен из **заголовка** или `?token=` → декодируем `Jwt` → кладём `userId` в `session.getAttributes()` → в хэндлере регистрируем сессию.

---

## REST API (шпаргалка)

> Все запросы — только c заголовком `Authorization: Bearer <access_token>`

### Пользователь

* `GET /api/users/me` — профиль текущего пользователя (из токена).
* `GET /api/users/{id}` — профиль по id (ограничения прав возможны).

### Комнаты

* `POST /api/rooms/direct-by-username?u1=alice&u2=bob` — создать личный диалог.
* `POST /api/rooms` — создать группу `{ "name": "Dev chat" }`.
* `POST /api/rooms/{roomId}/invite` — добавить участников `{ "usernames": ["bob","carol"] }`.
* `GET /api/rooms` — список комнат пользователя.

### Сообщения

* `POST /api/rooms/{roomId}/messages` — отправить сообщение:

  ```json
  { "type": "TEXT|ENCRYPTED|FILE", "content": "..." }
  ```

  *Отправитель берётся из токена.*
* `GET /api/rooms/{roomId}/messages?since=...` — история.

### Файлы (S3/MinIO)

* `POST /api/files/initiate` — начать multipart upload:

  ```json
  {
    "roomId": 1,
    "filename": "video.mp4",
    "sizeBytes": 734003200,
    "contentType": "video/mp4",
    "partSizeBytes": 8388608
  }
  ```
* `GET /api/files/{sessionId}/parts?from=1&count=10` — получить presigned PUT URLs для частей.
* `POST /api/files/{sessionId}/complete` — завершить multipart (список `partNumber+ETag`) → создаётся `Message(type=FILE)`.
* `POST /api/files/{sessionId}/abort` — отмена загрузки.
* `GET /api/files/{fileId}/download-url` — presigned GET для скачивания.

---

## WebSocket (протокол и тесты)

**Эндпоинт**: `ws://localhost:8080/ws/chat`
**Аутентификация**:
Header: `Authorization: Bearer <access_token>`
или Query: `?token=<access_token>`

### Входящие действия (к серверу)

```json
// отправить сообщение в комнату
{ "action":"sendMessage", "roomId": 1, "type":"ENCRYPTED", "content":"hello" }

// сигналинг WebRTC
{ "action":"callOffer",   "to": 42, "sdp": "..." }
{ "action":"callAnswer",  "to": 17, "sdp": "..." }
{ "action":"iceCandidate","to": 17, "candidate": { ... } }
```

### Исходящие события (с сервера)

```json
// новое сообщение
{ "event":"message", "roomId":1, "message": { ... } }

// файл
{ "event":"fileMessage", "roomId":1, "file": { ... } }

// сигналинг
{ "event":"callOffer",  "from": 17, "sdp": "..." }
{ "event":"callAnswer", "from": 42, "sdp": "..." }
{ "event":"iceCandidate","from": 42, "candidate": { ... } }
```

### Тест WS в Postman

1. Новая WebSocket-вкладка → URL `ws://localhost:8080/ws/chat`
2. Headers → `Authorization: Bearer {{TOKEN_ALICE}}` → **Connect**
3. Во второй вкладке — `{{TOKEN_BOB}}` → **Connect**
4. На вкладке Alice отправь:

   ```json
   { "action":"sendMessage","roomId":1,"type":"ENCRYPTED","content":"hello from WS" }
   ```

   Оба клиента должны получить `event: message`.

> Если получаешь `1011`, значит внутри обработчика выброшено исключение (например, пытались читать `userId` через `orElseThrow`). См. раздел «Отладка».

---

## Файлы: S3/MinIO, presigned + multipart

### Сценарий целиком

1. **Инициализировать** upload (REST): сервер проверяет права, создаёт UploadSession и S3 `uploadId`, возвращает `objectKey`, `bucket`, `partSize`, пресайны на первые N частей.
2. Клиент **загружает части** напрямую в S3/MinIO через **PUT presigned URL**, собирает `ETag` каждой части.
3. Клиент вызывает **complete** (REST), передавая `(partNumber, eTag)[]`. Сервер вызывает `CompleteMultipartUpload`, создаёт `FileMetadata`, публикует `fileMessage` в комнату (WS).
4. Скачивание — только по **presigned GET** (REST выдаёт URL с TTL).

### Почему сервис не «падает» при больших файлах

* Бэкенд **не проксирует трафик**. Он только выдаёт presigned URL и фиксирует метаданные.
* Нагрузка сети/диска — у S3/MinIO, горизонтально масштабируемого отдельно.
* На сервере ограничивайте: размер файла, число активных загрузок, дневные квоты.

---

## Звонки: WebRTC сигналинг

Только ретрансляция сигналинг-сообщений через WS:

* Alice → сервер: `callOffer` → сервер → Bob
* Bob → сервер: `callAnswer` → сервер → Alice
* Оба обмениваются `iceCandidate`.

Для локальной проверки удобно минимальный HTML-клиент (две вкладки, разные токены), где `RTCPeerConnection` отправляет SDP/ICE как JSON во WS. TURN-сервер (coturn) настраивается отдельно.

---

## Асинхронность и масштабирование

* События (новое сообщение/файл/сигналинг) публикуются в Redis Pub/Sub → другие инстансы доставляют в свои WS-сессии.
* Кэш Redis: профили/участие в комнатах (TTL 30–120 с).
* На балансировщике желательно **sticky session** (не обязательно, но упрощает).

---

## Производительность

* Минимизируйте полезную нагрузку WS-фреймов.
* Удерживайте обработку запроса <100 мс (кэш, батчи к БД, индексы).
* Presigned-подход для файлов разгружает бэкенд.
* Метрики Micrometer + `/actuator/health` для мониторинга.

---

## Отладка и типовые ошибки

### 1) WebSocket: 400/401/1011

* `400/401` при хэндшейке:
  — Заголовок `Authorization` содержит **два токена подряд** → оставьте **ровно один** `access_token`.
  — `issuer-uri` не совпадает с `iss` в токене; истёк `exp`; JVM не доверяет SSL Keycloak.
  — Включите логи:

  ```yaml
  logging.level.org.springframework.security=TRACE
  logging.level.org.springframework.web.socket=DEBUG
  ```

* `1011` сразу после `101 Switching Protocols`:
  — В `afterConnectionEstablished` делали `Optional.orElseThrow()` по `userId`, которого нет.
  — Исправьте: интерсептор **кладёт** `userId` в `session.getAttributes()`. В хэндлере — **никаких** `orElseThrow`, закрывайте сессию `1008` (policy violation) если `userId==null`.
  — На всякий случай сделайте fallback: извлекайте токен ещё раз из headers/`?token=` и декодируйте.


### 2) Keycloak/JWT

* `issuer-uri` **символ-в-символ** как `iss`.
* Не мапьте `aud`, если не добавили явную проверку.
* Убедитесь, что бэкенд может по HTTPS достучаться до `/.well-known/openid-configuration` и JWK.

---

## FAQ

**Почему нет `client_id/client_secret` в бэкенде?**
Потому что это **resource server**. Он не инициирует OAuth-потоки, а только проверяет пришедший `access_token` (подпись + валидность).

**Где берётся отправитель сообщения?**
Только из токена (`Jwt`) при запросе. Клиент **не** указывает `senderId`.

**Как защитить контент файлов?**
Бакет приватный, скачивание — только presigned GET с коротким TTL (сервер проверяет право доступа к комнате перед выдачей ссылки).

**E2E-шифрование реализовано на сервере?**
Нет. Сервер прозрачный: хранит шифротекст, публичные ключи, не имеет ключей расшифровки. Шифрование/дешифрование — задача клиентов.

**Выдержит ли сервис много больших файлов?**
Да, потому что байты идут **напрямую** в S3/MinIO по presigned URL. Сервис обрабатывает метаданные и события, а не поток данных.

---

Готов расширить README под твой репозиторий (добавить картинки MinIO/Keycloak, Postman-коллекцию и готовые Flyway-миграции). Скажи — подгоню под твоё точное дерево пакетов и текущие имена классов.
