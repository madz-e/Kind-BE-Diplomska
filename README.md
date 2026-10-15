# Kind — Backend

Spring Boot + PostgreSQL REST API for **Kind**, a mental wellness app. The React Native frontend lives in a separate repository (`Kind---Mental-Health-App-RN`).

Git repo Frontend: https://github.com/madz-e/Kind-FE-Diplomska
Git repo Backend: https://github.com/madz-e/Kind-BE-Diplomska
---

## Tech Stack

| Layer | Technology |
|---|---|
| Language / framework | Java 21, Spring Boot |
| Security | Spring Security, JWT (stateless), BCrypt password hashing |
| Persistence | Spring Data JPA / Hibernate, PostgreSQL |
| AI insights | Ollama (`qwen3:8b`) running locally; an OpenAI client is also available |
| Push notifications | Expo Push API (Firebase / FCM credentials live on expo.dev, not here) |
| Build | Maven (use the included `./mvnw` wrapper) |

---

## Prerequisites

- **JDK 21** (`java -version` should say 21)
- **PostgreSQL**, port **5432**. When the installer asks for a `postgres` password, use the one from the **Discord chat**.
- **Ollama** for AI mood insights: [ollama.com](https://ollama.com)

Maven isn't needed separately.

---

## 1. Create the database

The app needs a database named `kind_db`.

**Option A — pgAdmin:** Servers → PostgreSQL (enter the `postgres` password) → right-click **Databases** → Create → Database → `kind_db` → Save.

**Option B — terminal:**
```
psql -U postgres -c "CREATE DATABASE kind_db;"
```
If `psql` isn't recognized, call it by its full path. Check your version first:
```
dir "C:\Program Files\PostgreSQL"
```
then (replace `<ver>`):
```
& "C:\Program Files\PostgreSQL\<ver>\bin\psql.exe" -U postgres -c "CREATE DATABASE kind_db;"
```

Tables are created automatically on the first run (`ddl-auto=update`), so an empty database is fine.

---

## 2. Configuration

Config lives in `src/main/resources/`:

- **`application.properties`** activates the `prod` profile
- **`application-prod.properties`** holds the database, JWT and AI settings

If these files are missing after cloning, create them with the content below and fill in the real values from the **Discord chat**. **Never commit real passwords or keys.**

`application.properties`:
```properties
spring.profiles.active=prod
ollama.model=qwen3:8b
```

`application-prod.properties`:
```properties
# PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/kind_db
spring.datasource.username=postgres
spring.datasource.password=<DB_PASSWORD>
spring.datasource.hikari.connectionTimeout=20000
spring.datasource.hikari.maximumPoolSize=5

# JPA / Hibernate
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.properties.hibernate.jdbc.lob.non_contextual_creation=true
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# JWT
jwt.secret=<JWT_SECRET>
jwt.expiration=86400000

# AI
ollama.model=qwen3:8b
openai.api.key=${OPENAI_API_KEY:}
openai.model=${OPENAI_MODEL:gpt-4o-mini}
```

- `jwt.expiration` is in milliseconds (86400000 = 24 hours).
- The OpenAI key is optional and read from the `OPENAI_API_KEY` environment variable.

**Only if** you get a profile / "can't find datasource" error on startup: IntelliJ → Run Configuration for `KindBackendApplication` → **Active profiles** = `prod`.

---

## 3. Ollama (AI insights)

```
ollama pull qwen3:8b
```

Keep Ollama running while you use the app (it starts in the background after installing; otherwise run `ollama serve`). The Mood → Insights card uses it to write a short personalised insight.

---

## 4. Run

From IntelliJ: run `KindBackendApplication`.

From the terminal:
```
./mvnw spring-boot:run
```
(On Windows cmd: `mvnw.cmd spring-boot:run`)

The server starts on **http://localhost:8080**.

---

## 5. Seed data

The app relies on reference data: **emotions** (with mood categories), **mood factors** and **journal prompts**. Run `seed.py` to fill it in, and make sure your `emotion` table matches the rest of the team's (Mood → step 2 and the CBT feeling picker both read from it).

Mood values use a **1–5 scale**: 1 = Very Unpleasant, 2 = Unpleasant, 3 = Neutral, 4 = Pleasant, 5 = Very Pleasant.

---

## 6. Users & authentication

- Create users by **signing up in the app** (or `POST /api/register`). Passwords are hashed with BCrypt, so **don't insert users directly with SQL** — a plain-text password won't work for login.
- `POST /api/login` returns a JWT.
- Every other endpoint requires the header `Authorization: Bearer <token>`. Without a valid token the API returns **401** with a JSON error.
- The logged-in user is read from the token on the server, e.g. friends and the talk flag always act as the current user.

Public endpoints: `/api/register`, `/api/login`, `/error`.

---

## 7. Connect the frontend

In the frontend project, set the backend URL in its **`.env`** file:

```
API_URL=http://<your-laptop-ip>:8080/
```

- Find your IP with `ipconfig` → **Wireless LAN adapter Wi-Fi** → **IPv4 Address**.
- Phone and laptop must be on the **same Wi-Fi**.
- Allow inbound connections on **port 8080** in Windows Firewall (private networks).
- Restart the frontend dev server after changing `.env`.

CORS allows all origins, so the web version of the app can call the API as well.

---

## Project structure

```
src/main/java/com/example/
├── controller/       # REST endpoints (auth, mood, emotions, factors, journal, prompts,
│                     #   habits, habit logs, breathing sessions, friends, insights,
│                     #   affirmations, reminders, calming sounds, mindfulness, users)
│                     #   + GlobalExceptionHandler
├── dto/              # Request/response objects (auth, insights, mood patterns, habit weeks…)
├── jpaRepository/    # Spring Data JPA repositories
├── model/            # JPA entities
│   ├── enumerations/ # MoodCategory, EntryType, ExerciseType, JournalPromptType, ReminderType
│   └── exceptions/
├── security/         # SecurityConfig, JwtAuthFilter, JwtService
└── service/          # Service interfaces
    └── impl/         # Implementations, incl. MoodPatternService, InsightContextService,
                      #   OllamaClient, OpenAiClient, PushNotificationService
```

### Main features on the backend
- **Mood entries** with emotions and factors; **mood insights** combining rule-based checks (incl. crisis-phrase detection and low-mood streaks) with an LLM summary
- **Mood pattern analysis** over 30 days: habits vs. mood, factors vs. mood, habits vs. factors, and journal mentions
- **Journal** entries, prompts and CBT thought records
- **Habits** with daily logs and weekly progress
- **Friends** and the **talk flag**, with push notifications ("X wants to talk", "X sent you <3")
- **Breathing sessions** log

---

## Trying the API

`test.http` (with `http-client.env.json`) contains example requests you can run directly from IntelliJ.

---

## Troubleshooting

- `database "kind_db" does not exist` → step 1
- `Connection to localhost:5432 refused` → PostgreSQL isn't running
- Can't find datasource / profile error → step 2 (active profile `prod`)
- **401 Unauthorized** → missing or expired token; log in again (tokens last 24 hours)
- Login fails for a user created with SQL → users must be registered through the app or `/api/register`
- Insights fail / connection refused on port 11434 → Ollama isn't running, or `qwen3:8b` isn't pulled (step 3)
- Phone shows `Network request failed` → network/firewall, not code (step 7)

---

## Don't commit

- Real database password, JWT secret, or API keys