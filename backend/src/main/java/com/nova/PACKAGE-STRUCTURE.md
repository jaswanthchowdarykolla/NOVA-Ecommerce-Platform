NOVA backend package structure

com.nova
  NovaApplication.java
  config/       SecurityConfig, WebConfig, WebSocketConfig, AsyncConfig
  security/     JWT filter, JWT service, user-details service
  product/      Product entity, repository, controller, seed catalogue
  auth/         User entity, role, repository, DTOs, controller
  orders/       Order entity, repository, DTOs, controller
  payments/     Payment entity, repository, controller
  assistant/    Assistant DTOs and controller
  jobs/         BackgroundJob entity, repository, admin controller
  service/      ProductService, AuthService, OrderService, PaymentService,
                AssistantService, BackgroundJobService, BackgroundJobProcessor,
                JobNotificationService

Controllers are HTTP adapters. Services hold application/business logic and
coordinate repositories. Repositories encapsulate persistence. Configuration
and security are isolated from feature/domain packages.
