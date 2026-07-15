# Changelog

## 2026-07-14

### Changed

- Replaced shared global cart state with one server-side cart per HTTP session.
- Rebuilt checkout around authoritative persisted product/tax data, transactional stock locking, `BigDecimal` money, immutable order lines, and persisted receipts.
- Consolidated customer, order, catalog, image, and administrator route/service boundaries.
- Replaced entity-bound forms with validated request DTOs and canonical role handling.
- Reworked the storefront, cart drawer, checkout, authentication, and product administration UI for responsive layouts, keyboard operation, visible async states, semantic markup, and accessible labels.
- Replaced usernames with normalized email addresses across registration, customer login, administrator login, persistence, bootstrap configuration, and authentication.
- Matched authentication action widths, widened the storefront cart control, and redesigned administrator login/product creation as centered responsive cards with deliberate upload-area padding.
- Split development and production configuration; development uses local H2 while production uses environment-provided MySQL credentials.
- Added Flyway schema migrations, health endpoints, a private durable MySQL Compose service, and a reproducible non-root container image.
- Upgraded Spring Boot from 3.4.1 to 3.5.16 and removed the unused Lombok dependency/configuration.
- Normalized Java packages, separated request DTOs from persistence models, and added GitHub Actions test/container CI.
- Replaced placeholder documentation with architecture, setup, security, routes, screenshots, environment template, and MIT licensing.

### Fixed

- Removed public registration privilege injection, default administrator credentials, role rewrites, CSRF bypasses, duplicate email races, and overly broad admin authorization.
- Removed browser-authoritative pricing/tax/shipping, duplicate checkout creation, cross-session cart leakage, destructive schema recreation, debug logging, and committed credentials.
- Removed payment-card collection from the simulated checkout flow.
- Added safe decoded-image validation, generated filenames, upload size/dimension limits, and path containment.
- Removed duplicate routes, dead controllers/templates/scripts, generated repository junk, and committed merge markers.

### Verification

- Added integration coverage for registration privilege boundaries, case-insensitive duplicate emails, email-only authentication, session cart isolation, authoritative checkout totals, stock mutation, receipt persistence, and repeated empty checkout rejection.
- Exercised local H2 and containerized MySQL startup, email-based customer and administrator authentication, customer checkout, administrator image/product management, responsive layouts, health checks, and persistence across an application-container restart.
