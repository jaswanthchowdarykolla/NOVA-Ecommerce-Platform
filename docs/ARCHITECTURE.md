# NOVA architecture notes

## Request flow
- React routes render the storefront, catalogue, product details, cart, checkout, account, wishlist and admin views.
- `src/api/` contains transport-level API clients. `httpClient.ts` attaches the JWT when one is present.
- `src/services/` contains catalogue logic separate from UI components. Zustand stores own persisted cart/auth state.
- Spring controllers validate HTTP input and delegate to services. JPA repositories isolate database access.
- Authentication is stateless: BCrypt hashes passwords; `JwtService` issues/validates signed JWTs; `JwtAuthenticationFilter` establishes the security context; method-level authorization restricts mutations to ADMIN.
- Orders calculate totals from server-side product prices. The browser never supplies the order total as authoritative data.
- Razorpay order creation uses the server-side order amount. Checkout signatures and webhook signatures are checked using HMAC-SHA256. Payment success triggers background EMAIL, INVOICE and NOTIFICATION jobs.
- The AI assistant searches the persisted product catalogue. An optional OpenAI API key enables generated text; catalogue matching remains the fallback.

## Security notes
- Secrets are environment variables, never source literals.
- Public signup always creates CUSTOMER. Admin promotion is an operator action in the database.
- Order lookups are scoped to the authenticated user's email.
- Job history and the `/topic/jobs` WebSocket stream require an ADMIN JWT.
- Production deployments should additionally configure TLS, secret rotation, rate limiting, dependency scanning, payment idempotency, durable queueing and comprehensive authorization tests.


## Backend package convention

Business logic is centralized under `backend/src/main/java/com/nova/service/`: `ProductService`, `AuthService`, `OrderService`, `PaymentService`, `AssistantService`, `BackgroundJobService`, `BackgroundJobProcessor`, and `JobNotificationService`. Controllers, repositories, domain models, security, and configuration remain in their respective packages.
