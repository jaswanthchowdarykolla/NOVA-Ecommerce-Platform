# NOVA — E-commerce + AI Shopping Assistant

A separate Full Stack Web Engineering project. It uses the supplied NOVA UI direction (premium, futuristic commerce; champagne-gold accents; dark/light mode) and follows the layered frontend/backend patterns from SmartCart and the uploaded implementation guides. SmartCart itself is not overwritten.

## Technology stack
- **Frontend:** React 18, TypeScript, Vite, React Router, Zustand, Lucide, STOMP client for WebSocket job updates.
- **Backend:** Java 21, Spring Boot 3.4, Spring Web, Spring Data JPA, Spring Security, JWT (jjwt), validation, Actuator, WebSocket/STOMP, JavaMailSender, Razorpay REST integration.
- **Database:** PostgreSQL.
- **Local orchestration:** Docker Compose.
- **CI:** GitHub Actions workflow in `.github/workflows/ci.yml`.

## Project layout
```text
NOVA-Ecommerce/
├── frontend/
│   ├── src/api/            # Product, auth, order, payment, chat API clients
│   ├── src/api/            # Product, auth, order, payment and chat API clients
│   ├── src/components/     # Product card, chat helpers and admin job monitor
│   ├── src/hooks/useProducts.ts # Catalogue loading with local demo fallback
│   ├── src/pages/          # Home, listing, product, cart, checkout, auth, account, wishlist, admin
│   ├── src/schemas/        # Checkout validation
│   ├── src/services/       # Auth, order, assistant and product service logic
│   ├── src/store/          # Zustand auth and cart stores
│   ├── src/types.ts        # Product contract
│   ├── src/App.tsx         # Shared shell, navigation, routing and chat launcher
│   └── src/styles.css      # Premium responsive UI and theme styles
├── backend/
│   ├── src/main/java/com/nova/
│   │   ├── assistant/      # Optional LLM + catalogue-backed fallback
│   │   ├── auth/           # Signup, signin, user, role and DTOs
│   │   ├── config/         # HTTP security, CORS and WebSocket/STOMP config
│   │   ├── jobs/           # BackgroundJob entity, repository and admin controller
│   │   ├── orders/         # Order model, repository, DTOs and controller
│   │   ├── payments/       # Payment model, repository and controller
│   │   ├── product/        # Product model, repository, controller and seed catalogue
│   │   ├── security/       # JWT service, filter and user-details service
│   │   └── service/        # Product, auth, order, payment, assistant and job services
│   ├── src/main/resources/application.properties # Environment-based configuration
│   ├── Dockerfile
│   └── mvnw / mvnw.cmd
├── database/schema.sql
├── docker-compose.yml
└── .env.example
```

## Run locally (recommended: Docker Desktop)
1. Install Docker Desktop and Node.js 20+ / Java 21 if running parts outside Docker.
2. Copy `.env.example` to `.env`. Change the database password and replace `JWT_SECRET` with a random secret of at least 32 bytes. Leave optional AI/payment/mail variables blank until you configure them.
3. Copy `frontend/.env.example` to `frontend/.env` for local frontend development.
4. From this folder, run `docker compose up --build`.
5. Open the storefront at `http://localhost:5173`; API health is at `http://localhost:8080/api/health`; Actuator health is at `http://localhost:8080/actuator/health`.

### Run services separately
**Database:** start PostgreSQL on port 5432 and create database/user matching your environment variables.

**Backend (PowerShell):**
```powershell
cd backend
$env:SPRING_DATASOURCE_URL='jdbc:postgresql://localhost:5432/nova'
$env:SPRING_DATASOURCE_USERNAME='nova'
$env:SPRING_DATASOURCE_PASSWORD='your-local-database-password'
$env:JWT_SECRET='replace-with-a-random-secret-at-least-32-bytes-long'
.\mvnw.cmd spring-boot:run
```

**Frontend (PowerShell, second terminal):**
```powershell
cd frontend
npm install
npm run dev
```

The backend seeds 1,000 demo catalogue products when the products table is empty. Product pagination is fetched across all available pages so newly added catalogue items remain discoverable. Product images are remote Unsplash URLs. The demo product catalogue is not real inventory.

## Main API routes
| Method | Route | Access | Purpose |
|---|---|---|---|
| GET | `/api/health` | Public | API health |
| GET | `/api/products` | Public | Search, category, price, sort and paging |
| GET | `/api/products/featured` | Public | Featured catalogue |
| GET | `/api/products/{id}` | Public | Product detail |
| POST | `/api/auth/signup` | Public | Register customer; passwords hashed with BCrypt |
| POST | `/api/auth/signin` | Public | Sign in and issue JWT |
| POST | `/api/assistant/chat` | Public | Catalogue recommendations; optional LLM response |
| POST | `/api/orders` | JWT | Create pending-payment order |
| GET | `/api/orders/mine` | JWT | Current user's orders |
| POST | `/api/payments/create-order?orderId={id}` | JWT | Create Razorpay checkout order |
| POST | `/api/payments/verify` | JWT | Verify checkout signature and mark order paid |
| POST | `/api/payments/webhook` | Razorpay signature | Process `payment.captured` webhook |
| GET | `/api/jobs` | ADMIN JWT | Background-job history |
| WS | `/ws-nova` | ADMIN JWT in STOMP CONNECT | Live job updates at `/topic/jobs` |
| GET | `/actuator/health` | Public | Health probe |
| GET | `/actuator/metrics` | ADMIN JWT | Metrics |

## Authentication and admin setup
Signup always creates a `CUSTOMER`; the public signup request cannot assign an ADMIN role. To promote a trusted account locally, register normally and then run the SQL in `scripts/promote-admin.sql` against the `nova` database (replace the example email first):
Sign out and sign back in to obtain a JWT containing the updated role. Do not expose admin promotion to the public UI.

## AI assistant configuration
The assistant always has a catalogue-backed fallback, including matching on product name, brand, category, colour/tags and a basic budget ceiling. To enable model-generated responses, set `OPENAI_API_KEY` and optionally `OPENAI_MODEL` in the backend environment. The assistant is instructed to ground recommendations in current catalogue matches and not invent price or availability. With no API key, the service responds using catalogue search only.

## Razorpay setup (test mode only until reviewed)
Set `RAZORPAY_KEY_ID`, `RAZORPAY_KEY_SECRET`, and `RAZORPAY_WEBHOOK_SECRET` as environment variables. Configure the Razorpay webhook to `https://<public-backend-host>/api/payments/webhook` and subscribe to `payment.captured`. Do not use live keys until the merchant account and deployment are approved. Payment verification and webhook signatures are checked server-side. Never commit credentials.

## Email and background processing
After a payment is verified, NOVA creates EMAIL, INVOICE, and NOTIFICATION jobs. Jobs are processed asynchronously; job status is persisted and broadcast over STOMP to the ADMIN monitor. An HTML invoice is written under `backend/invoices/` (working directory dependent). To send email, configure `MAIL_USERNAME`, `MAIL_PASSWORD` (use a provider app password), and optionally `MAIL_FROM`. If SMTP is not configured, email jobs fail explicitly while invoice/notification jobs can still complete. `@Async` and the in-memory STOMP broker are learning-scale components, not a durable distributed queue.

## Deployment notes
- **Frontend:** deploy `frontend/` to Vercel and configure `VITE_API_BASE_URL` to the public backend's `/api` URL. `frontend/vercel.json` enables SPA route fallback.
- **Backend:** deploy the Dockerfile-backed Spring Boot API to Render or Railway. Set database, JWT, CORS, AI, payment and mail secrets in the provider's environment settings. Set `APP_CORS_ORIGINS` to the deployed frontend origin.
- **Database:** use a managed PostgreSQL instance for deployed environments; do not rely on an expiring free learning database for long-term storage.
- **CI:** GitHub Actions builds the frontend and packages the backend on pushes/PRs. Deployment-specific CD still needs to be connected in the hosting provider.
- **Observability:** Actuator health/info/metrics endpoints and structured application logging are configured. Keep non-health Actuator endpoints restricted to admins.

## Scope and remaining production work
- This is a full-stack course-project baseline, not a production-certified commerce system.
- The Admin UI includes product create/update/delete controls and the background-job monitor. Backend endpoints independently enforce ADMIN authorization.
- Saved-address management, password reset, stock reservation/rollback, refunds, cancellation workflow, rate limiting, fraud controls and production incident handling are outside this course-project scope and would be required before operating a real store.
- Payment keys, webhook registration, SMTP and optional AI key are external setup requirements; without them the related features cannot complete.
- WebSocket job monitoring is restricted to ADMIN JWTs, but the configuration still requires a production security review before public exposure.
- Build verification must be performed in an environment with dependency access. Run `npm install && npm run build` in `frontend/` and `mvnw.cmd test` in `backend/` (PowerShell), or `./mvnw test` on macOS/Linux. The backend test profile uses H2 so the context test does not require a running PostgreSQL instance.


Backend business logic is organized in `backend/src/main/java/com/nova/service/`, with separate controller, repository, domain, security, and configuration packages.
