# INT² — Intern Intelligence

INT² is a private, single-user cybersecurity company discovery and cold-outreach tool. This repository currently contains the Phase 1 project bootstrap: a Next.js frontend, a Spring Boot backend, Python ingestion placeholders, Docker development configuration, and a Terraform workspace placeholder.

## Repository layout

- `frontend/` — Next.js, TypeScript, Tailwind CSS
- `backend/` — Java 21 and Spring Boot REST API
- `ingestion/` — Python NACSA ingestion and enrichment packages
- `infrastructure/terraform/` — GCP infrastructure (implemented in Phase 14)

## Prerequisites

- Node.js 20+
- Docker Desktop (recommended for the Java 21 backend)
- Java 21 and Maven 3.9+ only if running the backend outside Docker
- Python 3.12+ for later ingestion work

## Local setup

1. Copy `.env.example` to `.env` and replace placeholder values when integrations are implemented.
2. Copy `frontend/.env.local.example` to `frontend/.env.local`.
3. Run the frontend:

   ```powershell
   Set-Location frontend
   npm install
   npm run dev
   ```

4. Run the backend with Docker:

   ```powershell
   docker compose up --build backend
   ```

5. Open `http://localhost:3000`; backend health is at `http://localhost:8080/api/health`.

The backend connects to PostgreSQL and applies Flyway migrations at startup. Production uses Cloud SQL through the Cloud SQL Java connector; local development can use any PostgreSQL instance through the three `SPRING_DATASOURCE_*` values in `.env`. To add development-only sample records, run `backend/scripts/dev-seed.sql` against a development database; never run it in production.

Email generation uses Gemini by default. Set `LLM_PROVIDER=gemini`, `GEMINI_API_KEY`, and optionally `GEMINI_MODEL` (default `gemini-2.5-flash`). The previous OpenAI integration remains available with `LLM_PROVIDER=openai` and `LLM_API_KEY`. Provider keys stay in the backend environment and must never use a `NEXT_PUBLIC_*` name.

CV uploads are stored privately in Google Cloud Storage at `candidate/cv.pdf`. Terraform provisions the bucket and grants the backend service account object access. For local uploads, set `GCP_CANDIDATE_BUCKET` and use Application Default Credentials or `GOOGLE_APPLICATION_CREDENTIALS`.

## Whole-stack Docker run

```powershell
docker compose up --build
```

No secrets should be committed. The example files contain placeholders only.

## Company API

- `GET /api/companies` — paginated list; filters: `search`, `state`, `city`, `service`, `nacsaLicence`, `contacted`
- `GET /api/companies/{id}` — full company details
- `GET /api/companies/map` — lightweight map records with the same filters
- `PATCH /api/companies/{id}/contacted` — body: `{ "contacted": true }`
- `GET /api/candidate` — safe candidate metadata; extracted CV text is never returned
- `POST /api/candidate/cv` — multipart PDF upload (`file`), maximum 10 MB
- `POST /api/email/generate` — body: `{ "companyId": "..." }`; generates and stores a grounded draft
