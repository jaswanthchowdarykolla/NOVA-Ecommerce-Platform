# NOVA build and acceptance checklist

Use this checklist before presenting or deploying the application.

## Required environment
- [ ] Docker Desktop (recommended) or PostgreSQL 16.
- [ ] Java 21 for local backend execution.
- [ ] Node.js 20+ and npm.
- [ ] Copy `.env.example` to `.env`; set a strong database password and a random `JWT_SECRET` of at least 32 bytes.
- [ ] Do not commit `.env`, API keys, mail app passwords, or live payment keys.

## Build checks
- [ ] `docker compose config` validates the Compose file.
- [ ] `docker compose up --build` starts PostgreSQL, backend, and frontend.
- [ ] `GET http://localhost:8080/api/health` returns `status: ok`.
- [ ] `GET http://localhost:8080/api/products?page=0&size=24` returns product records.
- [ ] `npm install && npm run build` passes in `frontend/`.
- [ ] `./mvnw test` (or `mvnw.cmd test` on Windows) passes in `backend/`.

## Functional smoke tests
1. Register a customer with a password of at least 8 characters.
2. Sign in; confirm the JWT is stored under `nova-token` and the account page opens.
3. Search the catalogue, open a product, add it to the cart, adjust quantity, and reload to confirm cart persistence.
4. Add products to and remove products from the wishlist.
5. Submit checkout details. Confirm an order is created as `PENDING_PAYMENT` before payment confirmation.
6. With Razorpay TEST keys configured, complete a test payment and confirm the server verifies the signature and changes the order to `PAID`.
7. Confirm EMAIL, INVOICE and NOTIFICATION jobs are created after verified payment. Email jobs require SMTP configuration; invoice jobs write HTML files to the backend working directory.
8. Promote a trusted test account using `scripts/promote-admin.sql`, sign in again, and verify product create/update/delete controls work only for ADMIN.
9. Confirm the Admin job monitor loads history and receives STOMP updates at `/topic/jobs`.
10. With `OPENAI_API_KEY` absent, assistant requests should use catalogue search; with a valid key, model responses may be used with catalogue grounding.

## Known external dependencies
- Product imagery is served from Unsplash URLs and requires network access in the browser.
- Razorpay checkout and webhooks require test credentials and a publicly reachable webhook URL (for example, a temporary tunnel during local testing).
- Email confirmation requires SMTP credentials. Without these, the email background job correctly records `FAILED`; invoice and notification jobs are independent.
- The in-memory Spring STOMP broker and `@Async` executor are appropriate for a learning/demo deployment, not durable multi-instance production processing.
