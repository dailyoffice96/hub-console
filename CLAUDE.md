# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project overview

sm-console is an internal admin console (member/CS management, incident tracking, audit logs) with a
Spring Boot backend and a React frontend as two independent projects in one repo, deployed separately
(backend on its own host, frontend on Vercel per `frontend/vercel.json`).

- `backend/` — Spring Boot 4.1 (Java 17), Maven
- `frontend/` — React 19 + Vite, plain JS (no TypeScript)

## Commands

### Backend (`backend/`)

```
./mvnw spring-boot:run              # run the API on port 9000
./mvnw test                         # run all tests
./mvnw test -Dtest=ClassName        # run a single test class
./mvnw test -Dtest=ClassName#method # run a single test method
./mvnw package                      # build the jar
```

Requires a MySQL instance reachable via `DB_HOST`/`DB_NAME`/`DB_USER`/`DB_PASSWORD` env vars
(see `application.properties`). `spring.jpa.hibernate.ddl-auto=update` currently manages the schema;
Flyway is on the classpath with a migration under `src/main/resources/db/migration/` but is disabled
(`spring.flyway.enabled` is commented out) — enabling it and reconciling it with `ddl-auto=update`
needs care.

### Frontend (`frontend/`)

```
npm run dev        # Vite dev server (http://localhost:5173)
npm run build       # production build
npm run lint         # ESLint
npm run preview      # preview the production build
```

There is no frontend test runner configured.

## Architecture

### Backend: package-by-feature, not layer-by-layer

Under `backend/src/main/java/com/smconsole/`, code is grouped by domain, not by layer — each feature
package (`admin`, `user`, `incident`, `inquiry`, `auditlog`, `statistics`, `systemsetting`, `excel`,
`externalapi`, `notification`, `ai`) holds its own entity, repository, service, controller, and
request/response DTOs together. Cross-cutting config lives in `config/` (Spring Security, WebSocket,
Redis cache, seed data) and `common/exception/` (global `@RestControllerAdvice`).

Key domains:
- **admin** — the operators of this console (`Admin`/`AdminRole`: SUPER_ADMIN/ADMIN/STAFF). Session-based
  login via Spring Security form login (`SecurityConfig`), not the `user` domain.
- **user** — end users of the product being managed (members), separate from `admin`.
- **incident** — outage/incident tracking with a status history and severity, reachable both from the
  admin UI and via an unauthenticated `POST /api/incidents/webhook` (see `SecurityConfig` permit list)
  for external systems to report incidents.
- **inquiry** — CS/support tickets with comments and status history, assignable to an admin.
- **auditlog** — records admin actions (`AuditAction`/`AuditTargetType`); services call into
  `AuditLogService` directly (no AOP) when they mutate something worth auditing. `AuditLogAnalyzePage`
  on the frontend sends log data to `OpenAiService` for AI-assisted analysis.

**Optimistic locking without JPA `@Version` exceptions**: `Incident`/similar entities use a manual
`findByIdAndVersion(id, version)` repository lookup instead of relying on JPA's automatic version-check
exception. A missing row means a stale version, and the service throws `IllegalStateException`, which
`GlobalExceptionHandler` maps to 409 Conflict. The exception handler also has a (currently unused) handler
for the JPA-native `ObjectOptimisticLockingFailureException`, kept as a fallback.

**Auth model**: session/cookie-based (not JWT). `SecurityConfig` disables CSRF, enables CORS with
credentials for the known frontend origins (localhost:5173, localhost:9000, the Vercel deploy), and uses
form login at `/login` with `loginId`/`password`. `LoginSuccessHandler`/`LoginFailureHandler` also enforce
a maintenance-mode lock (via `SystemSetting`) that blocks non-SUPER_ADMIN logins during a configured
window, and reset/track failed-login counts on `Admin`.

**Real-time updates**: STOMP over WebSocket (`WebSocketConfig`), endpoint `/ws` with SockJS fallback,
simple broker on `/topic`, app prefix `/app`. Consumed on the frontend in `IncidentMonitoringPage.jsx` via
`@stomp/stompjs`/`sockjs-client` for live incident updates.

**Excel export**: `excel/` package (Apache POI) generates downloadable reports for users and audit logs.

### Frontend

- Routing is centralized in `routes/AppRoutes.jsx`; `routes/PrivateRoute.jsx` is meant to gate authenticated
  pages but `hooks/useAuth.js` is currently a stub (`isLoggedIn` never gets set from a real session check —
  a `/me`-style check is a `TODO` in that file).
- `api/` holds one axios wrapper module per backend domain (`adminApi`, `usersApi`, `incidentApi`,
  `inquiryApi`, `auditLogApi`, `authApi`, `systemsettingApi`), all built on the shared
  `api/axiosInstance.js` (`baseURL: http://localhost:9000`, `withCredentials: true` for the session
  cookie). The base URL is hardcoded for local dev — check this before assuming it works against a
  deployed backend.
- Pages (`pages/`) are one per route/domain and pull in feature-specific modal components
  (`components/*Modal.jsx`) for create/detail/edit flows; `components/Layout.jsx` + `Sidebar.jsx` +
  `Header.jsx` provide the authenticated shell.
- Charting uses `chart.js`/`react-chartjs-2`; styling mixes Bootstrap/react-bootstrap with custom CSS
  (`App.css`, `css/Login.css`).

## Notes for future changes

- `backend/src/main/resources/application.properties` currently has a live-looking OpenAI API key and other
  values committed in plaintext. Treat this as sensitive — do not print, log, or propagate it, and flag it
  back to the user rather than assuming it's a placeholder.
- Korean-language comments throughout the backend explain intent line-by-line (e.g. `SecurityConfig`,
  `DataInitializer`) — match that convention when editing those files rather than stripping the comments.
