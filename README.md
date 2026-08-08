# DIT Parking Manager

Parking management system for **Dar es Salaam Institute of Technology (DIT)** — a university OOP course project.

**Live:** [https://oop.overssh.com](https://oop.overssh.com)

Stack: **Spring Boot 3.5 (Java 21)** · **PostgreSQL** · **React 19 + TypeScript + Vite** · **shadcn/ui**

---

## Features

- Session login (admin / customer)
- Vehicle CRUD management (car, truck, motorcycle)
- Slot CRUD management with Standard / VIP / Disabled bays
- Check-in (auto or manual slot), ticket issue
- Check-out with fee calculation, then payment (cash / card / mobile money)
- History and admin reports (revenue, occupancy)

Money is always in **Tanzanian Shillings (TSh)** using `BigDecimal`.

| Vehicle | Rate / hour | Slot size |
| --- | --- | --- |
| Car | 2,000 | MEDIUM |
| Truck | 5,000 | LARGE |
| Motorcycle | 1,000 | SMALL |

VIP slots multiply the rate by **1.5**. Partial hours round **up**.

---

## OOP principles (where to look)

| Principle | Where in code |
| --- | --- |
| **Encapsulation** | `Vehicle.setPlateNumber` (validates `T123ABC`); `ParkingSlot.occupy()` / `release()` (no public `available` setter); `ParkingSession.close()` sets fee + exit together; `User.admin()` / `User.customer()` factories (no role setter) |
| **Abstraction** | `Payment.process()` — `PaymentService` calls it without knowing whether money arrives as cash, card, or mobile money |
| **Inheritance** | JOINED hierarchy: `Vehicle` → `Car` / `Truck` / `Motorcycle` |
| **Polymorphism** | `ParkingSession.calculateFee()` = `vehicle.hourlyRate() × slot.rateMultiplier() × hours`; each `Vehicle` subclass prices and sizes itself |

Only `Vehicle` is a class hierarchy. Users, slots and payments are one table each,
with a type/role/method column deciding behaviour:

```
Vehicle (abstract)        users         role   = ADMIN | CUSTOMER
├─ Car                    parking_slot  type   = STANDARD | VIP | DISABLED
├─ Truck                                available
└─ Motorcycle             payment       method = CASH | CARD | MOBILE_MONEY
```

---

## Project structure

```
parking/
├── pom.xml                          # Spring Boot backend
├── src/main/java/tz/ac/dit/parking/
│   ├── ParkingApplication.java
│   ├── domain/                      # JPA entities + OOP hierarchies
│   ├── repository/                  # Spring Data JPA
│   ├── service/                     # Business rules
│   ├── web/                         # REST controllers + DTOs
│   ├── config/                      # Security + seed data
│   └── exception/                   # AppException
├── src/main/resources/
│   ├── application.properties       # Local (gitignored)
│   └── application.properties.example
├── frontend/                        # React SPA
│   ├── public/dit-logo.png          # DIT crest
│   ├── src/
│   │   ├── api/                     # fetch client + types
│   │   ├── auth/                    # AuthContext, route guard
│   │   ├── components/              # shell + shadcn/ui
│   │   ├── pages/                   # Lot map, vehicles, check-in/out…
│   │   └── lib/utils.ts
│   └── package.json
└── README.md
```

### API surface

| Method | Path | Who |
| --- | --- | --- |
| POST | `/api/auth/login` | public |
| POST | `/api/auth/logout` | authenticated |
| GET | `/api/auth/me` | authenticated |
| GET/POST | `/api/vehicles` | scoped by role |
| PUT/DELETE | `/api/vehicles/{id}` | owner/admin |
| GET | `/api/slots` | authenticated |
| POST/PATCH | `/api/slots` | admin |
| DELETE | `/api/slots/{code}` | admin |
| POST | `/api/sessions/check-in` | gated by `canOperate` |
| POST | `/api/sessions/{id}/check-out` | gated by `canOperate` |
| GET | `/api/sessions` | scoped by role |
| POST | `/api/payments` | gated by `canOperate` |
| GET | `/api/reports/revenue` | admin |
| GET | `/api/reports/occupancy` | admin |

---

## Database / “migrations”

There is **no Flyway/Liquibase**. Schema is managed by Hibernate:

```properties
spring.jpa.hibernate.ddl-auto=update
```

On startup Hibernate creates/updates tables from the `@Entity` classes. Only the
`Vehicle` hierarchy is JOINED (one table per subclass); users, slots and payments
are a single table each — **9 tables total**:

`users` · `vehicle` + `car` / `truck` / `motorcycle` · `parking_slot` ·
`parking_session` · `ticket` · `payment`

`SeedDataRunner` loads demo users, vehicles, and ~20 slots **once** (skips if admin `shemsa` already exists).

### Postgres setup

```bash
# create DB (adjust credentials to match your install)
sudo -u postgres psql -c "CREATE USER postgres WITH PASSWORD 'postgres';"   # if needed
sudo -u postgres psql -c "CREATE DATABASE parking OWNER postgres;"
```

Copy config:

```bash
cp src/main/resources/application.properties.example \
   src/main/resources/application.properties
# edit URL / username / password if needed
```

Default local settings:

| Setting | Value |
| --- | --- |
| Host | `127.0.0.1:5432` |
| Database | `parking` |
| User / password | `postgres` / `postgres` |
| API port | `8082` |

To reset schema for a clean demo:

```bash
psql -h 127.0.0.1 -U postgres -c "DROP DATABASE parking;"
psql -h 127.0.0.1 -U postgres -c "CREATE DATABASE parking;"
# then restart the backend — tables + seed recreate
```

---

## How to run (local)

### Prerequisites

- Java **21**
- Node.js **20+**
- PostgreSQL **14+** with database `parking`

### Backend

```bash
cp src/main/resources/application.properties.example \
   src/main/resources/application.properties

./mvnw spring-boot:run
# API → http://localhost:8082
```

Or build a jar:

```bash
./mvnw -DskipTests package
java -jar target/parking-0.0.1-SNAPSHOT.jar
```

### Tests

```bash
./mvnw test
```

18 tests: a context-load check plus `ApiSmokeTest`, which drives the real HTTP
layer, security and JPA against an **in-memory H2 database** — no Postgres
needed, nothing to set up. It covers login and roles, slot type rules
(disabled-permit, VIP reservation), the check-in → check-out → pay cycle,
all three payment methods with their decline paths, and role scoping.

### Frontend

```bash
cd frontend
npm install
npm run dev
# Vite → http://localhost:5173  (proxies /api → :8082)
```

Production frontend build:

```bash
cd frontend
npm run build
# output in frontend/dist/
```

---

## Seeded accounts

| Username | Password | Role | Notes |
| --- | --- | --- | --- |
| `shemsa` | `password` | Admin | **Shemsa Amin** |
| `juma` | `password` | Customer | Owns car + truck |
| `neema` | `password` | Customer | Disability permit; owns motorcycle + car |

Demo payments: cash needs enough tendered; **card ending in `0` is declined**; mobile money always succeeds.

---

## Typical flow

1. Sign in as `juma`
2. **Check in** a vehicle (auto slot)
3. **Check out** → status becomes `AWAITING_PAYMENT`, fee shown
4. **Pay** (cash / card / mobile money) → session `CLOSED`, bay free
5. Sign in as `shemsa` for **Reports**

---

## Production notes

Deployed behind **Caddy** at `oop.overssh.com`:

- Static files from `frontend/dist`
- `/api/*` reverse-proxied to Spring Boot on `127.0.0.1:8082`
- systemd unit: `parking.service`

```bash
# rebuild & restart API
./mvnw -DskipTests package
sudo systemctl restart parking

# rebuild SPA (Caddy serves frontend/dist)
cd frontend && npm run build
```

---

## Tech choices (student-friendly)

- Hand-written getters/setters on domain classes (no Lombok there) so encapsulation is visible
- One `AppException` instead of many exception types
- One end-to-end smoke suite (`ApiSmokeTest`) over the real HTTP + security + JPA stack, rather than many isolated unit tests with mocks
- Entities never leave the service layer — controllers return DTOs only
