# Reservation System

Система бронирования комнат, разработанная на Spring Boot с использованием современных практик enterprise-разработки.

## 📋 Описание

Это REST API для управления бронированиями комнат. Система поддерживает создание, обновление, отмену и одобрение бронирований с защитой от конфликтов и race conditions.

### Основные возможности

- ✅ CRUD операции для бронирований
- ✅ Проверка доступности комнат
- ✅ Workflow управления статусами (PENDING → APPROVED/CANCELLED)
- ✅ Защита от конфликтующих бронирований
- ✅ Защита от race conditions через optimistic и pessimistic locking
- ✅ Пагинация и фильтрация
- ✅ Валидация бизнес-правил на уровне domain model

## 🏗️ Архитектура

Проект использует **feature-based packaging** и **layered architecture**:
```
com.reserv.reservation_system/
├── common/
│   └── exception/          # Глобальная обработка ошибок
├── reservation/
│   ├── api/                # REST контроллеры и DTO
│   ├── availability/       # Проверка доступности комнат
│   ├── domain/             # Domain модели и бизнес-логика
│   ├── persistence/        # JPA entities и repositories
│   └── service/            # Сервисный слой
```

### Слои приложения

1. **API Layer** (`api/`) - REST endpoints, HTTP обработка
2. **Service Layer** (`service/`) - Бизнес-логика, оркестрация
3. **Domain Layer** (`domain/`) - Бизнес-модели с инвариантами
4. **Persistence Layer** (`persistence/`) - Доступ к данным

## 🛠️ Технологический стек

- **Java 21**
- **Spring Boot 3.x**
- **Spring Data JPA** - работа с БД
- **Hibernate** - ORM
- **PostgreSQL** - СУБД
- **Docker & Docker Compose** - контейнеризация
- **Maven** - сборка проекта

## 🚀 Запуск проекта

### Предварительные требования

- Java 21+
- Docker и Docker Compose
- Maven 3.8+

### Запуск через Docker Compose

1. Собрать jar-файл:
```bash
mvn clean package -DskipTests
```

2. Запустить контейнеры:
```bash
docker-compose up -d
```

Приложение будет доступно на `http://localhost:8080`

### Запуск локально (без Docker)

1. Запустить PostgreSQL:
```bash
docker run -d \
  --name postgres \
  -e POSTGRES_PASSWORD=root \
  -p 5432:5432 \
  postgres:latest
```

2. Настроить переменные окружения (создать `.env`):
```properties
DB_URL=jdbc:postgresql://127.0.0.1:5432/postgres
DB_USER=postgres
DB_PASSWORD=root
SERVER_PORT=8080
```

3. Запустить приложение:
```bash
mvn spring-boot:run
```

## 📡 API Endpoints

### Управление бронированиями

#### Создать бронирование
```http
POST /reservation
Content-Type: application/json

{
  "userId": 1,
  "roomId": 5,
  "startDate": "2026-03-10",
  "endDate": "2026-03-15"
}
```

Статус автоматически устанавливается в `PENDING`.

#### Получить бронирование по ID
```http
GET /reservation/{id}
```

#### Получить список бронирований с фильтрацией
```http
GET /reservation?roomId=5&userId=1&pageSize=10&pageNumber=0
```

Параметры (все опциональные):
- `roomId` - фильтр по комнате
- `userId` - фильтр по пользователю
- `pageSize` - размер страницы (по умолчанию 10)
- `pageNumber` - номер страницы (с 0)

#### Обновить бронирование
```http
PUT /reservation/{id}
Content-Type: application/json

{
  "userId": 1,
  "roomId": 5,
  "startDate": "2026-03-12",
  "endDate": "2026-03-17"
}
```

⚠️ Можно обновить только бронирования в статусе `PENDING`.

#### Отменить бронирование
```http
DELETE /reservation/{id}/cancel
```

⚠️ Нельзя отменить уже одобренные (`APPROVED`) бронирования.

#### Одобрить бронирование
```http
POST /reservation/{id}/approve
```

Проверяет отсутствие конфликтов с другими одобренными бронированиями перед одобрением.

### Проверка доступности

#### Проверить доступность комнаты
```http
POST /reservation/availability/check
Content-Type: application/json

{
  "roomId": 5,
  "startDate": "2026-03-10",
  "endDate": "2026-03-15"
}
```

Ответ:
```json
{
  "message": "Room is available for reservation",
  "status": "AVAILABLE"
}
```

## 🔒 Защита от конфликтов

Система использует многоуровневую защиту от race conditions:

### 1. Optimistic Locking
Каждая `ReservationEntity` имеет поле `@Version`:
```java
@Version
@Column(name = "version")
private Long version;
```

Защищает от одновременного изменения одной брони несколькими запросами.

### 2. Pessimistic Locking
При одобрении бронирования используется `SELECT FOR UPDATE`:
```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
List<ReservationEntity> findAndLockConflictingReservations(...)
```

Блокирует конфликтующие брони на время проверки, предотвращая одобрение пересекающихся бронирований.

### 3. Database Constraints (опционально)
PostgreSQL exclusion constraint для дополнительной защиты:
```sql
CREATE EXTENSION IF NOT EXISTS btree_gist;

ALTER TABLE reservations 
ADD CONSTRAINT no_overlapping_approved_reservations 
EXCLUDE USING gist (
    room_id WITH =,
    daterange(start_date, end_date, '[]') WITH &&
)
WHERE (status = 'APPROVED');
```

## 📊 Модель данных

### Reservation

| Поле | Тип | Описание |
|------|-----|----------|
| id | Long | Уникальный идентификатор (auto-generated) |
| userId | Long | ID пользователя |
| roomId | Long | ID комнаты |
| startDate | LocalDate | Дата начала бронирования |
| endDate | LocalDate | Дата окончания бронирования |
| status | ReservationStatus | Статус бронирования |
| version | Long | Версия для optimistic locking |

### Статусы бронирования (ReservationStatus)

- `PENDING` - Создана, ожидает одобрения
- `APPROVED` - Одобрена
- `CANCELLED` - Отменена

### Переходы между статусами
```
PENDING ──approve──> APPROVED
   │
   └──cancel──> CANCELLED

CANCELLED (конечное состояние)
APPROVED (можно отменить только через поддержку)
```

## ✅ Бизнес-правила и валидация

### Валидация в Domain Model
```java
public record Reservation(...) {
    public Reservation {
        if (!endDate.isAfter(startDate)) {
            throw new IllegalArgumentException(
                "End date must be after start date"
            );
        }
    }
}
```

### Правила бронирования

1. ✅ `endDate` должна быть строго после `startDate`
2. ✅ Даты должны быть в будущем или сегодня (`@FutureOrPresent`)
3. ✅ При создании `status` должен быть `null` (устанавливается автоматически)
4. ✅ Можно изменять только брони в статусе `PENDING`
5. ✅ Нельзя одобрить бронь, если есть конфликт с другими `APPROVED` бронями
6. ✅ Нельзя отменить уже одобренную бронь (требуется обращение в поддержку)

## 🐛 Обработка ошибок

Все ошибки возвращаются в едином формате:
```json
{
  "messege": "Bad request",
  "erroeMessege": "End date must be after start date",
  "errorTime": "2026-02-08T14:30:00"
}
```

### HTTP статусы

| Код | Описание |
|-----|----------|
| 200 | Успешная операция |
| 201 | Бронирование создано |
| 400 | Некорректные данные (валидация не прошла) |
| 404 | Бронирование не найдено |
| 409 | Конфликт (optimistic lock, конфликтующие брони) |
| 500 | Внутренняя ошибка сервера |

### Примеры ошибок

#### Некорректные даты
```json
{
  "messege": "Bad request",
  "erroeMessege": "End date must be after start date",
  "errorTime": "2026-02-08T14:30:00"
}
```

#### Бронирование не найдено
```json
{
  "messege": "Entity not found",
  "erroeMessege": "No Reservation with id: 999",
  "errorTime": "2026-02-08T14:30:00"
}
```

#### Конфликт при одобрении
```json
{
  "messege": "Bad request",
  "erroeMessege": "Can't approve reservation because of conflict",
  "errorTime": "2026-02-08T14:30:00"
}
```

#### Optimistic Lock Exception
```json
{
  "messege": "Reservation was modified by another request. Please retry.",
  "erroeMessege": "...",
  "errorTime": "2026-02-08T14:30:00"
}
```

## 🗄️ База данных

### Создание таблицы
```sql
CREATE TABLE reservations (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    room_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_reservations_room_dates 
ON reservations(room_id, start_date, end_date);

CREATE INDEX idx_reservations_user 
ON reservations(user_id);

CREATE INDEX idx_reservations_status 
ON reservations(status);
```

### Миграции

Добавление поля version для optimistic locking:
```sql
ALTER TABLE reservations 
ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
```

Добавление exclusion constraint (опционально):
```sql
CREATE EXTENSION IF NOT EXISTS btree_gist;

ALTER TABLE reservations 
ADD CONSTRAINT no_overlapping_approved_reservations 
EXCLUDE USING gist (
    room_id WITH =,
    daterange(start_date, end_date, '[]') WITH &&
)
WHERE (status = 'APPROVED');
```

## 🧪 Тестирование

### Примеры curl запросов

**Создать бронирование:**
```bash
curl -X POST http://localhost:8080/reservation \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "roomId": 5,
    "startDate": "2026-03-10",
    "endDate": "2026-03-15"
  }'
```

**Проверить доступность:**
```bash
curl -X POST http://localhost:8080/reservation/availability/check \
  -H "Content-Type: application/json" \
  -d '{
    "roomId": 5,
    "startDate": "2026-03-10",
    "endDate": "2026-03-15"
  }'
```

**Одобрить бронирование:**
```bash
curl -X POST http://localhost:8080/reservation/1/approve
```

**Получить все бронирования комнаты:**
```bash
curl "http://localhost:8080/reservation?roomId=5&pageSize=20"
```

## 🔧 Конфигурация

### Переменные окружения

| Переменная | Описание | Пример |
|------------|----------|--------|
| `DB_URL` | JDBC URL базы данных | `jdbc:postgresql://localhost:5432/postgres` |
| `DB_USER` | Пользователь БД | `postgres` |
| `DB_PASSWORD` | Пароль БД | `root` |
| `SERVER_PORT` | Порт приложения | `8080` |

### application.properties
```properties
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

server.port=${SERVER_PORT}
```

## 📚 Используемые паттерны и практики

### Архитектурные паттерны
- **Layered Architecture** - разделение на слои (API, Service, Domain, Persistence)
- **Feature-based Packaging** - группировка по функциональности, а не по техническому назначению
- **Repository Pattern** - абстракция доступа к данным
- **Domain Model Pattern** - бизнес-логика в domain объектах

### Принципы проектирования
- **SOLID** принципы
  - Single Responsibility: каждый класс имеет одну ответственность
  - Dependency Inversion: зависимости через интерфейсы
- **DRY (Don't Repeat Yourself)** - валидация в domain model, без дублирования
- **Fail Fast** - валидация на входе, ранний возврат ошибок

### Практики безопасности данных
- **Optimistic Locking** - защита от lost updates
- **Pessimistic Locking** - защита от race conditions при бизнес-логике
- **Transaction Management** - атомарность операций
- **Domain Invariants** - невозможность создать невалидный объект

### Обработка ошибок
- **Global Exception Handler** - централизованная обработка исключений
- **Consistent Error Format** - единый формат ошибок для клиентов
- **Proper HTTP Status Codes** - корректные коды ответов

## 🚧 Возможные улучшения

### Функциональные
- [ ] Аутентификация и авторизация (Spring Security)
- [ ] Роли пользователей (USER, ADMIN)
- [ ] История изменений бронирований (audit log)
- [ ] Email уведомления об изменении статуса
- [ ] Поиск альтернативных доступных дат
- [ ] Блокировка комнат на техническое обслуживание
- [ ] Recurring reservations (повторяющиеся брони)

### Технические
- [ ] Unit и Integration тесты
- [ ] API документация (Swagger/OpenAPI)
- [ ] Metrics и мониторинг (Actuator, Prometheus)
- [ ] Логирование (структурированное, ELK stack)
- [ ] Миграции БД (Flyway/Liquibase)
- [ ] Кэширование (Redis) для проверки доступности
- [ ] Rate limiting для API endpoints
- [ ] CI/CD pipeline

### Архитектурные
- [ ] Разделение на микросервисы (User Service, Room Service, Reservation Service)
- [ ] Event-driven architecture (Kafka для событий бронирования)
- [ ] CQRS для разделения чтения и записи
- [ ] API Gateway
- [ ] Service mesh
