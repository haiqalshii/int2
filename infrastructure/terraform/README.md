# INT2 Google Cloud infrastructure

This directory defines the production infrastructure for INT2: Cloud SQL for PostgreSQL, a private Cloud Storage bucket, required APIs, Artifact Registry, a dedicated Cloud Run identity, Secret Manager metadata/IAM, both Cloud Run services, and optional GitHub deployment triggers.

## Adopt the existing deployment

The resources already exist in `project-int2`. Import them before applying:

```powershell
cd infrastructure/terraform
terraform init
Copy-Item terraform.tfvars.example terraform.tfvars
.\import-existing.ps1
terraform plan
```

Review the plan. The intended migration replaces the default Compute Engine runtime identity with the dedicated `int2-cloud-run` service account.

For a new Cloud SQL instance, set the built-in `postgres` user's password to the value stored in `int2-db-password`, and set `int2-db-user` to `postgres`:

```powershell
gcloud sql users set-password postgres --instance int2-postgres --prompt-for-password --project project-int2
```

The backend's JDBC URL is generated from the Cloud SQL connection name. Cloud Run connects with the dedicated runtime identity; no database host or Supabase secrets are required.

## Secret values

Terraform deliberately does not manage secret versions because their plaintext would remain in Terraform state. Existing versions continue working after import. Rotate a value with:

```powershell
$value = Read-Host "New value" -AsSecureString
$plain = [System.Net.NetworkCredential]::new('', $value).Password
$plain | gcloud secrets versions add int2-app-password --data-file=- --project project-int2
$plain = $null
```

## Validate and apply

```powershell
terraform fmt -check
terraform validate
terraform plan -out=tfplan
terraform apply tfplan
```

The deployed image is ignored after bootstrap so commit-SHA deployments do not create Terraform drift. Both services have deletion protection.

## Automatic GitHub deployments

Connect this repository in **Cloud Build > Repositories** using the Google Cloud Build GitHub app, then set the following in `terraform.tfvars` and apply:

```hcl
enable_github_triggers = true
github_owner           = "haiqalshii"
github_repository      = "int2"
```

A push to `main` touching `backend/**` or `frontend/**` then builds and deploys only that service.

Local Terraform state is ignored by Git. For team use, bootstrap a dedicated GCS state bucket and add a `backend "gcs"` block to `versions.tf`.
