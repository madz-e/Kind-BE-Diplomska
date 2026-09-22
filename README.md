# Kind — Backend Setup

Spring Boot + PostgreSQL backend. Fresh clone → running server.

## Prerequisites
- **JDK 21** (`java -version` should say 21)
- **PostgreSQL** — install from [postgresql.org](https://www.postgresql.org/download/), keep port **5432**, keep pgAdmin checked. When it asks for a `postgres` user password, use the one in the **Discord chat**.

Maven isn't needed separately — use the included `./mvnw` wrapper.

## 1. Create the database
App needs a database named `kind_db`. Do **either** option:

**Option A — pgAdmin:** open pgAdmin → expand Servers → PostgreSQL (enter the `postgres` password) → right-click **Databases** → Create → Database → name it `kind_db` → Save.

**Option B — terminal:**
```
psql -U postgres -c "CREATE DATABASE kind_db;"
```
If `psql` isn't recognized, it's not on your PATH — call it by full path. First check which Postgres version you have:
```
dir "C:\Program Files\PostgreSQL"
```
That prints your version number (e.g. `16`, `17`). Use it in the path (replace `<ver>`):
```
& "C:\Program Files\PostgreSQL\<ver>\bin\psql.exe" -U postgres -c "CREATE DATABASE kind_db;"
```
Or just use Option A.

Tables auto-create on first run, so empty is fine.

## 2. Check credentials
In `src/main/resources/application-prod.properties` the DB username is `postgres`. The password must match your local `postgres` password — the agreed one is in the **Discord chat**.

## 3. Profile
An `application.properties` is already in the repo that activates the `prod` profile, so the DB config loads automatically — you shouldn't need to do anything.

**Only if** you get a profile / "can't find datasource" error on startup: in IntelliJ open the Run Configuration for `KindBackendApplication` → set **Active profiles** to `prod` → apply.

## 4. Create a test user
Endpoints need a user (no auth yet). After the backend has started at least once (so the tables exist):
```sql
INSERT INTO kind_users (email, password, first_name, last_name, completed_intro, notification_enabled)
VALUES ('test@test.com', 'test123', 'Test', 'User', true, false);
```
The frontend hardcodes user id `1`, so this user should be id 1. Check with:
```sql
SELECT id FROM kind_users;
```

## 5. Point the frontend at this backend
The app reads the backend URL from `src/config/environments.js` (in the frontend project `Kind---Mental-Health-App-RN`).

**See the current value:** open `src/config/environments.js`, find the `local` block, look at `base_api_url`.

**Change it** depending on how you run the app:
- **Android emulator:** `base_api_url: 'http://10.0.2.2:3000/'`
- **Physical phone (Expo Go):** use your laptop's LAN IP. Find it: run `ipconfig`, look under **Wireless LAN adapter Wi-Fi** for **IPv4 Address** (e.g. `192.168.1.42`). Then set:
  ```js
  base_api_url: 'http://192.168.1.42:3000/',   // your IP, keep :3000 and trailing slash
  ```
  Phone and laptop must be on the same network. If it won't connect: use a laptop **Mobile Hotspot** and join the phone to it (laptop IP is then usually `192.168.137.1`), set the network profile to **Private**, and allow inbound TCP 3000 in the firewall.

**After changing it:** save the file and reload the app (press `r` in the Metro terminal, or shake the phone → Reload).

**Check it works:** open `http://<your-ip>:3000/api/mood-entries/user/1` in the phone's browser → should return `[]` (empty list = success).

## Known scaffolding
- No auth yet — endpoints open, FE hardcodes `user: { id: 1 }`. Replace with logged-in user once auth exists.
- Mood/journal save + read back work; showing results in the UI (vs mock data) is still pending.

## Recent backend changes
- `JournalEntry.content`: `@Lob` → `@Column(columnDefinition = "TEXT")` (reads back as normal text on Postgres).
- Added `@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})` to lazy relationships on `MoodEntry` and `JournalEntry` (fixes JSON serialization).

## Troubleshooting
- `database "kind_db" does not exist` → do step 1.
- `Connection to localhost:5432 refused` → Postgres not running.
- Fails to find DB config → `prod` profile not active (step 3).
- `User not found` on save → do step 4, id must match what FE sends (`1`).
- Phone `Network request failed` → network/firewall, not code (see step 5).