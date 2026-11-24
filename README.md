# poultry-crm-backend

## Configuration

Set `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` in the environment before running the application. Do not commit credentials; local `.env` files are ignored by Git.

## Generated reports

Before deploying the report-generation feature to an existing database, run `src/main/resources/reporting-schema.sql` once against the application's PostgreSQL database. This creates the persistent report-job history table and indexes and updates the supported report types.

Generated PDF files are written to `REPORT_STORAGE_DIR` when configured, or to `./storage/reports` by default. The directory must be on persistent server storage and writable by the application process. PostgreSQL stores report metadata and the generated file path, not the PDF bytes.

### Report API

- `POST /api/reports/generate` — queue a PDF report and return HTTP 202 with its report ID.
- `GET /api/reports` — list the authenticated user's latest 100 reports.
- `GET /api/reports/{id}` — retrieve status and report metadata.
- `GET /api/reports/{id}/download` — download a completed PDF.
- `POST /api/reports/{id}/retry` — requeue a failed report.

Supported report types: `SALES`, `PURCHASE`, `PAYMENTS`, `OUTSTANDING`, `DELIVERIES`, and `TRIP_SUMMARY`. Date ranges are required for all reports except `TRIP_SUMMARY`, which requires a positive `tripId` and uses the trip's date automatically. The maximum date range for other reports is five years. Optional filters are `tripId` and `partyId`; payment reports also accept `paymentType` and `status`. For outstanding reports, `toDate` is the as-of date.

Example date-range request:

```json
{
  "reportType": "SALES",
  "fromDate": "2026-11-01",
  "toDate": "2026-11-21",
  "format": "PDF",
  "filters": {
    "partyId": 12,
    "tripId": 42
  }
}
```

Example Trip Summary request:

```json
{
  "reportType": "TRIP_SUMMARY",
  "format": "PDF",
  "filters": {
    "tripId": 125
  }
}
```

Trip Summary includes the trip header, active trip orders, payments, and expenses. Its PDF uses the existing asynchronous report job, status, download, and retry workflow.

Jobs are claimed from PostgreSQL using `FOR UPDATE SKIP LOCKED`, so multiple application instances can share the queue without claiming the same queued job. Report history and files are private to the authenticated user who requested them.
