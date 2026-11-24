# Poultry CRM backend

Spring Boot API for the Poultry CRM application.

## Runtime configuration

Configure these values through the deployment environment or the backend `.env` file:

- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` — PostgreSQL connection
- `CORS_ALLOWED_ORIGINS` — comma-separated trusted browser origins; use the deployed HTTPS frontend origin
- `REPORT_STORAGE_DIR` — persistent directory for generated report files
- `REPORT_WORKER_POLL_INTERVAL_MS` — report worker polling interval
- `BOOTSTRAP_ADMIN_ENABLED` — optional one-time default-tenant admin bootstrap; defaults to `false`
- `BOOTSTRAP_ADMIN_USERNAME`, `BOOTSTRAP_ADMIN_PASSWORD` — required only when bootstrap is enabled
- `SERVER_PORT` — deployment port; defaults to `1111`

Do not commit secrets. Put the API behind HTTPS before accepting Basic authentication credentials.

## Database migrations

Flyway is enabled at startup.

- A fresh database runs `V1__baseline_schema.sql` followed by `V2__shared_tenant_isolation.sql`.
- An existing non-empty `demo` schema without Flyway history is baselined at version 1 and receives V2.
- Existing records are assigned to the `default` tenant to preserve the current installation.
- Back up PostgreSQL before deploying the migration.

## Shared-schema multi-tenancy

Each organization has a tenant record. Business entities and generic business records carry a `tenant_id`; Hibernate's discriminator tenancy filters ORM reads and writes using the authenticated account's tenant. SQL-backed finance and report queries include explicit tenant predicates because native SQL is not automatically tenant-filtered.

Create an organization and its first administrator with:

`POST /api/tenants/register`

Request body:

```json
{
  "companyName": "Example Poultry Ltd",
  "ownerName": "Organization Owner",
  "username": "owner@example.com",
  "password": "Use-a-unique-password-12"
}
```

Usernames remain globally unique because the current HTTP Basic login identifies accounts by username. The bootstrap admin is disabled by default; new organizations can register without a shared default password.

## Frontend business data API

The frontend's generic directory/workflow adapter uses:

- `GET /api/data/{resource}`
- `POST /api/data/{resource}`
- `PUT /api/data/{resource}/{id}`
- `DELETE /api/data/{resource}/{id}`

Records are persisted in `demo.business_record` and isolated by tenant. Existing trip, finance, employee, vehicle, and report endpoints remain in place.
