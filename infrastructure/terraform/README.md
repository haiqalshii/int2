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

## Bootstrap Cloud SQL

The deploying account needs Cloud SQL Admin (`roles/cloudsql.admin`) in the project. A project IAM administrator can grant it with:

```powershell
gcloud projects add-iam-policy-binding project-int2 `
  --member="user:YOUR_ACCOUNT_EMAIL" `
  --role="roles/cloudsql.admin"
```

Create the protected database resources before updating Cloud Run:

```powershell
terraform apply `
  -target=google_sql_database_instance.postgres `
  -target=google_sql_database.app
```

After the instance exists, configure the two database secrets. `int2-db-user` must contain exactly `postgres`. Choose one database password and use that identical value for both `int2-db-password` and the Cloud SQL `postgres` user.

```powershell
$userFile = New-TemporaryFile
[IO.File]::WriteAllText($userFile.FullName, 'postgres', [Text.UTF8Encoding]::new($false))
gcloud secrets versions add int2-db-user "--data-file=$($userFile.FullName)" --project=project-int2
Remove-Item $userFile.FullName

$databasePassword = Read-Host 'Choose database password' -AsSecureString
$plainPassword = [System.Net.NetworkCredential]::new('', $databasePassword).Password
$passwordFile = New-TemporaryFile
[IO.File]::WriteAllText($passwordFile.FullName, $plainPassword, [Text.UTF8Encoding]::new($false))
gcloud secrets versions add int2-db-password "--data-file=$($passwordFile.FullName)" --project=project-int2
Remove-Item $passwordFile.FullName
$plainPassword = $null
```

Now assign that same chosen password to Cloud SQL. Enter it again when prompted:

```powershell
gcloud sql users set-password postgres --instance int2-postgres --prompt-for-password --project project-int2
```

Finally, run the complete plan and apply described below. The backend's JDBC URL is generated from the Cloud SQL connection name. Cloud Run connects with the dedicated runtime identity; no database host or Supabase secrets are required.

## Secret values

Terraform deliberately does not manage secret versions because their plaintext would remain in Terraform state. Existing versions continue working after import. Check that every required secret has an enabled version without printing its value:

```powershell
$requiredSecrets = @(
  'int2-api-key', 'int2-app-password', 'int2-db-password', 'int2-db-user',
  'int2-gemini-key', 'int2-google-maps-key', 'int2-session-token'
)
$requiredSecrets | ForEach-Object {
  $version = gcloud secrets versions list $_ --filter='state=ENABLED' --limit=1 --format='value(name)' --project=project-int2
  if ($version) { Write-Host "OK: $_ (version $version)" -ForegroundColor Green }
  else { Write-Host "MISSING ENABLED VERSION: $_" -ForegroundColor Red }
}
```

Do not run the final full apply while any secret reports `MISSING ENABLED VERSION`.

## Validate and apply

```powershell
terraform fmt -check
terraform validate
terraform plan -out=tfplan
terraform apply tfplan
```

## Monitoring

Set `monitoring_alert_email` in `terraform.tfvars`, then plan and apply. Terraform creates a one-minute external check of `/api/health` and alerts for two minutes of downtime, backend 5xx responses, and Cloud SQL CPU above 80 percent for five minutes. Google may ask the recipient to verify a newly created email notification channel.

Failed uptime probes are written to Cloud Logging. View active incidents under **Google Cloud Console > Monitoring > Alerting** and check configuration under **Monitoring > Uptime checks**.

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
