# URL Shortener — Node/Express → Spring Boot 4 port

This is a line-by-line reconstruction of a Node.js + Express + PostgreSQL +
Redis URL shortener, rebuilt on **Spring Boot 4.1.1 / Spring Framework 7 /
Java 26**. Same two endpoints, same JSON shapes, same static frontend
(untouched) — different runtime, different idioms. It's written to be
*read*, not just run: every file has comments pointing back at the exact
line of the original it replaces and explaining *why* the Spring/Java way
looks the way it does.

```
Original                          Spring Boot equivalent
─────────────────────────────────────────────────────────────────────
index.js (routes)              -> controller/UrlShortenerController.java
urlService.js                  -> service/UrlShortenerService.java
                                   service/UrlCacheService.java
pg.js (raw SQL via node-pg)    -> repository/*Repository.java (Spring Data JPA)
redis.js (manual GET/SETEX)    -> config/CacheConfig.java + @Cacheable/@CachePut
schema.sql                     -> src/main/resources/db/migration/V1__init_schema.sql (Flyway)
public/ (static frontend)      -> src/main/resources/static/ (byte-for-byte copy)
docker-compose.yml             -> same file, extended with an `app` service
(no tests)                     -> src/test/.../UrlShortenerApplicationTests.java (Testcontainers)
```

---

## 1. Stack & why these exact versions

| Piece | Version | Notes |
|---|---|---|
| Java | **26** | Current six-month release (non-LTS). Java **25** is the current LTS — see the "JDK 26 vs 25" callout below before you deploy this anywhere real. |
| Spring Boot | **4.1.1** | Latest stable as of writing. Built on **Spring Framework 7**, **Jakarta EE 11**, requires Java 17+. |
| Build tool | Maven (wrapper included, `./mvnw`) | |
| Web | Spring MVC (`spring-boot-starter-web`), embedded Tomcat | Undertow is **gone** in Boot 4 — it doesn't support the Servlet 6.1 baseline Boot 4 requires. |
| Persistence | Spring Data JPA + Hibernate 7 + PostgreSQL driver | |
| Schema management | **Flyway** (`db/migration/V1__init_schema.sql`) | Replaces `schema.sql` + manual `psql` runs |
| Cache | Spring Data Redis (Lettuce client) + Spring Cache abstraction | Replaces the hand-written `redisClient.get`/`setEx` calls |
| Validation | `spring-boot-starter-validation` (Jakarta Bean Validation) | Replaces the `if (!longURL)` check |
| JSON | **Jackson 3** (Boot 4 default) | See the dedicated section below — this bit us once while writing this |
| Docs | springdoc-openapi **3.0.0** (the line that supports Boot 4) | Swagger UI at `/swagger-ui.html` |
| Observability | Spring Boot Actuator | `/actuator/health`, `/actuator/metrics` — the Node app had none of this |
| Tests | JUnit 5 + Testcontainers (Postgres + Redis) | Spins up real, throwaway containers per test run |

---

## 2. Prerequisites

- **JDK 26** on your PATH (`java -version` should print 26). If you can't get
  a JDK 26 build for your OS yet, install **JDK 25 (LTS)** instead and change
  `<java.version>` in `pom.xml` and the two `FROM eclipse-temurin:26-...`
  lines in the `Dockerfile` to `25` — Spring Boot 4.1 supports both.
- **Docker** (for Postgres/Redis via `docker-compose.yml`, and for
  Testcontainers-based tests).
- No local Maven install needed — use the bundled `./mvnw` / `mvnw.cmd`.

---

## 3. Running it

### Option A — infra in Docker, app from your IDE (fastest inner loop)

```bash
docker compose up postgres-db redis-cache -d
./mvnw spring-boot:run
```

App comes up on **http://localhost:9000**. Flyway runs the migration
automatically on startup — you don't run `schema.sql` by hand anymore.

### Option B — everything in Docker

```bash
docker compose up --build
```

This also builds and runs the Spring Boot app itself (see the `app` service
in `docker-compose.yml`), wired to talk to the other two containers by
service name instead of `localhost`.

### Environment variables (all optional — sensible localhost defaults exist)

| Variable | Default | Used for |
|---|---|---|
| `DB_HOST` / `DB_PORT` / `DB_NAME` / `DB_USER` / `DB_PASSWORD` | `localhost` / `5433` / `url` / `postgres` / `postgres` | Postgres connection |
| `REDIS_HOST` / `REDIS_PORT` | `localhost` / `6379` | Redis connection |
| `APP_BASE_URL` | `http://localhost:9000` | Prefix used when building the `shortURL` in responses |

---

## 4. API — unchanged from the original

```
POST /api/v1/data
  body:    { "longURL": "https://example.com/very/long/path" }
  201:     { "shortURL": "http://localhost:9000/a1b2c3d4" }   (newly created)
  200:     { "shortURL": "http://localhost:9000/a1b2c3d4" }   (already existed — idempotent)
  400:     { "error": "Invalid URL" }

GET /{shortUrl}
  302 redirect to the original long URL, and records a click_analytics row
  404: { "error": "Short URL not found: ..." }
```

The static page at `/` (copied byte-for-byte from `public/index.html`)
calls `POST /api/v1/data` exactly as before — **it required zero changes**.

Swagger UI: `http://localhost:9000/swagger-ui.html`
Actuator health: `http://localhost:9000/actuator/health`

---

## 5. Things to understand, not just copy

This is the part worth reading slowly — it's where the actual "reverse
engineering" learning happens.

### 5.1 Everything is a bean, and beans are wired, not required

Node's `import { x } from "./file.js"` is a direct reference to a specific
module. Spring's equivalent — constructor injection via
`@RequiredArgsConstructor` (Lombok) + `@Service`/`@Repository`/`@RestController`
— wires objects together *by type* at startup, through an
`ApplicationContext`. You never call `new UrlShortenerService()` yourself;
Spring builds it and hands it to whatever asks for it. This indirection is
what makes annotations like `@Transactional` and `@Cacheable` possible in
the first place (see 5.3).

### 5.2 Spring Data JPA repositories vs raw SQL

`pg.js` exported one function, `query(text, params)`, and every SQL string
lived in `urlService.js`. Here, `UrlMappingRepository extends
JpaRepository<UrlMapping, Long>` gives you `save()`, `findById()` etc. for
free, and methods like `findByLongUrl(String)` have their SQL **generated
from the method name** at startup — no annotation needed for something
this simple. Reach for `@Query` only once a method name would get
unreasonably long.

### 5.3 The self-invocation proxy trap (`UrlCacheService`)

This is the single most important gotcha in this codebase, and one of the
most common ways people get burned by Spring caching/transactions in real
jobs. `@Cacheable`, `@CachePut`, and `@Transactional` all work by wrapping
your bean in a runtime **proxy**. The proxy is what intercepts a method
call to check the cache (or open a transaction) before your code runs —
but only when the call comes from **outside** the bean, through the proxy.

If `UrlShortenerService.createOrGetShortUrl()` called
`this.findShortUrlByLongUrl(...)` directly, that's a plain Java call on
`this` that never touches the proxy — `@Cacheable` would be silently
ignored. No error, no log line, it just never caches. That's why the
cached read/write methods live on their own bean, `UrlCacheService`, and
`UrlShortenerService` calls them through a normal `@Autowired`-style
constructor reference. Crossing a real bean boundary means the call goes
through the proxy correctly.

*(The "proper" alternative fixes: self-injection of your own proxy via
`@Lazy`, or `AopContext.currentProxy()` with `exposeProxy=true` on
`@EnableAspectJAutoProxy`. Splitting into two beans, like here, is usually
the cleanest.)*

### 5.4 Why the redirect route is `/{shortUrl:[0-9a-f]{8}}`, not `/{shortUrl}`

`HashUtils` always produces exactly 8 lowercase hex characters (both the
SHA-256 slice and the random-4-bytes collision fallback). The controller's
`@GetMapping` constrains the path variable to that exact pattern instead of
matching anything. This isn't just validation — without it, this catch-all
route would **also match `/index.html` and `/img.jpg`**, because Spring
MVC's `@RequestMapping` handlers are consulted before the static-resource
handler and would win the match. The original Express app didn't have this
problem because `app.use(express.static("public"))` ran *before* the
`app.get("/:shortURL", ...)` route was registered. Spring MVC's precedence
rules are different, and this is a very easy bug to ship silently — the
homepage would 404 the moment someone added a catch-all route.

### 5.5 Cache-aside, spelled out as annotations

The original pattern:
```js
const cached = await redisClient.get(key);
if (cached) return cached;
const value = await queryDb();
if (value) await redisClient.setEx(key, TTL, value);
return value;
```
becomes one annotation:
```java
@Cacheable(cacheNames = "urls-by-long", key = "#longUrl", unless = "#result == null")
public String getShortUrlByLongUrl(String longUrl) { ... }
```
`unless = "#result == null"` is what stops Spring from caching a cache-miss
forever — equivalent to the `if (cached) ...` / `if (value) await setEx`
checks. `CacheConfig` sets the 24h TTL to match `CACHE_TTL_SECONDS = 86400`
from `urlService.js`, and gives the two caches the same effective
`long:`/`short:` key-namespace split via two named `RedisCacheConfiguration`s
(`urls-by-long`, `urls-by-short`).

### 5.6 Records instead of object literals

`ShortenRequest`, `ShortenResponse`, `ErrorResponse`, and
`UrlShortenerService.ShortenResult` are all Java `record`s — the closest
thing the language has to a JS object literal, but immutable and typed.
`@JsonProperty("longURL")` on the record component maps Java's
`longUrl` field to the JSON key `longURL` the *existing, unmodified*
frontend already sends — a small but important detail when you're wiring
a new backend to an old frontend.

### 5.7 Validation moves out of the handler

The `if (!longURL) return res.status(400)...` / `try { new URL(longURL) }
catch` pair in `index.js` becomes: `@NotBlank` on the DTO field (triggers
automatically on `@Valid @RequestBody`) plus a small `validate()` method
using `java.net.URI` (the modern, non-DNS-resolving URL-shaped type —
`java.net.URL`'s `equals()`/`hashCode()` do a DNS lookup, which is exactly
the kind of legacy footgun you don't want in "recommended" code anymore).
Both failure paths land in `GlobalExceptionHandler`, which is Spring's
answer to scattering `try/catch` + `res.status(x).json({error})` across
every route — one `@RestControllerAdvice` class handles it for the whole
app.

### 5.8 Virtual threads: the concurrency model actually changed

```yaml
spring:
  threads:
    virtual:
      enabled: true
```
Node's single-threaded event loop handles concurrent requests by never
blocking (`await` yields the loop). Java traditionally handled it by
pooling OS threads, which is expensive per-thread. Since Java 21, **virtual
threads** (Project Loom) give you the best of both: write normal blocking
code (`repository.save(...)` just blocks the current thread like it always
has), but the JVM parks the *virtual* thread instead of the underlying OS
thread while waiting on I/O — so you get event-loop-like scalability
without restructuring your code into callbacks or reactive types. For an
I/O-bound service like this one (mostly waiting on Postgres and Redis),
this is arguably the single biggest concurrency-model upgrade over the
Node original, and it's a one-line config change.

### 5.9 Jackson 3 — Boot 4's biggest "gotcha" upgrade

Spring Boot 4 defaults to **Jackson 3**, not Jackson 2. The core classes
moved package **and** Maven group from `com.fasterxml.jackson.*` to
`tools.jackson.*` — e.g. `com.fasterxml.jackson.databind.ObjectMapper`
becomes `tools.jackson.databind.json.JsonMapper`. The one deliberate
exception: **`jackson-annotations`** (which is what gives you `@JsonProperty`,
used in `ShortenRequest`/`ShortenResponse` here) **keeps** its old
`com.fasterxml.jackson.annotation` package on purpose, so 2.x and 3.x code
can share the same annotations. If you ever add custom JSON
serialization/deserialization logic beyond simple annotations, expect to
import from `tools.jackson.*`, not the `com.fasterxml.jackson.databind.*`
you'll find in most existing tutorials and Stack Overflow answers — most
of the internet's Jackson content still targets 2.x.

This project deliberately keeps `CacheConfig` on `StringRedisSerializer`
for both cache keys/values (since both are plain `String`s) specifically
to sidestep this — no `ObjectMapper`/`JsonMapper` version juggling needed
for something this simple. If you extend this to cache whole objects,
that's when you'd reach for Spring Data Redis's
`GenericJacksonJsonRedisSerializer` (Jackson 3) — check the exact class
name against whatever `spring-data-redis` version is actually on your
classpath, since this naming shuffled around the Boot 4 release.

### 5.9b Boot 4 also relocated its own autoconfigure "customizer" interfaces

Separately from Jackson: Spring Boot 4 split its old monolithic
`spring-boot-autoconfigure` module into smaller, per-feature autoconfigure
modules, and a number of `*BuilderCustomizer`/`*Customizer` interfaces moved
packages as part of that split. `RedisCacheManagerBuilderCustomizer` is one
example — it lived in `org.springframework.data.redis.cache` (a Spring
*Data* Redis package) on Boot 3, and moved to
`org.springframework.boot.cache.autoconfigure` (a Spring *Boot* package) on
Boot 4. The class itself didn't change, just its package — but the compiler
error is a bare "cannot find symbol" with no hint about where the class
went. If you hit that error on any Boot 4 project, the fix is almost always
"search for the class name and find its new package", not "the feature was
removed".

### 5.10 Flyway instead of "run this .sql file manually"

`schema.sql` was a file you had to remember to run against a fresh
database. `V1__init_schema.sql` under `db/migration/` is a **numbered,
version-controlled migration** — Flyway runs it automatically on startup,
tracks what's already been applied in a `flyway_schema_history` table, and
refuses to re-run it. The rule going forward: never edit an already-applied
migration file; add `V2__whatever.sql` instead.

### 5.11 200 vs 201 — a detail worth noticing

The original returns `res.json(...)` (implicit 200) when the long URL was
already shortened, and `res.status(201).json(...)` only for a brand-new
mapping. `UrlShortenerService.ShortenResult` carries a `newlyCreated`
flag specifically so the controller can pick the right status code without
querying twice — a small example of designing a return type around what
the *caller* needs, not just what's convenient to compute.

---

## 6. Testing

```bash
./mvnw test
```

`UrlShortenerApplicationTests` uses **Testcontainers** to start real,
disposable Postgres and Redis containers for the test run (requires a
running Docker daemon) — no shared "test database" to accidentally
pollute, and no mocking of the database/cache layer, so you're testing
real SQL and real cache behavior. `@ServiceConnection` is what wires
Spring's `DataSource`/`RedisConnectionFactory` to point at the containers
automatically.

---

## 7. What's genuinely new here (not in the original)

- `GET /actuator/health`, `/actuator/metrics` — production readiness the
  Node app never had.
- `ClickAnalyticsRepository.findByShortUrlOrderByTimestampDesc(...)` — a
  one-line query ready for a future "clicks for this link" endpoint.
- Bean Validation error messages instead of a generic `"No URL given"`.
- Swagger/OpenAPI docs generated from the controller annotations.
- A real test suite.

## 8. Known simplifications / good next exercises

- **Click recording is still synchronous** — `resolveAndRecordClick` saves
  the analytics row and returns, same as the original `await
  recordClickInDb(...)` before the redirect. A good exercise: make it
  `@Async` (Spring's thread-pool-backed fire-and-forget) so a slow
  analytics insert never delays the 302. You'll need `@EnableAsync` on the
  main class and to think about what happens if the async write fails
  silently.
- **No rate limiting** on `POST /api/v1/data` — anyone can hammer it.
  Bucket4j + Redis is a natural fit given Redis is already in the stack.
- **No auth** on the analytics data — same as the original, but worth
  flagging before this goes anywhere near production.
- **IP address storage** (`ClickAnalytics.ip`) has privacy/GDPR
  implications the original never addressed either — worth researching if
  you extend this into something real.
- **JDK 26 vs JDK 25 (LTS)** — this project uses 26 because it's the
  newest available toolchain, which is a fine choice for *learning on the
  bleeding edge*. For anything you'd actually deploy and maintain, prefer
  the current LTS (25) — non-LTS releases get a much shorter support
  window before you're forced to upgrade again.

---

## 9. Project layout

```
src/main/java/dev/singularity/urlshortener/
├── UrlShortenerApplication.java     entry point, @EnableCaching
├── config/CacheConfig.java         Redis cache TTL + serializers
├── controller/                     REST endpoints (thin — no business logic)
├── dto/                            request/response records
├── entity/                         JPA entities (urls, click_analytics)
├── exception/                      custom exceptions + @RestControllerAdvice
├── repository/                     Spring Data JPA interfaces
├── service/                        business logic + the cache-proxy split
└── util/HashUtils.java             SHA-256 hashing, collision fallback

src/main/resources/
├── application.yml                 all config, env-var overridable
├── db/migration/V1__init_schema.sql   Flyway schema
└── static/                         untouched frontend (index.html, img.jpg)
```

## 10. A note on how this was built

This conversion could not be compiled or run inside the environment that
generated it (no access to Maven Central to resolve dependencies there).
Every file was written and reviewed carefully against current Spring Boot
4.1 / Jackson 3 / Testcontainers documentation, and the known gotchas above
(self-invocation, static-resource routing, Jackson package changes) were
specifically checked for rather than assumed away — but **run `./mvnw test`
yourself before trusting this in anything beyond a learning context**. If
something doesn't compile on your machine, it's most likely a dependency
version drift (check `pom.xml` against Maven Central for whatever's newest
when you're reading this) rather than a logic error.
