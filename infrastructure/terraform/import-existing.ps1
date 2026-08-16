$ErrorActionPreference = 'Stop'
$project = 'project-int2'
$region = 'asia-southeast1'

$imports = [ordered]@{
  'google_artifact_registry_repository.containers' = "projects/$project/locations/$region/repositories/cloud-run-source-deploy"
  'google_cloud_run_v2_service.backend' = "projects/$project/locations/$region/services/int2-backend"
  'google_cloud_run_v2_service.frontend' = "projects/$project/locations/$region/services/int2-frontend"
}

@('artifactregistry.googleapis.com', 'cloudbuild.googleapis.com', 'iam.googleapis.com', 'run.googleapis.com', 'secretmanager.googleapis.com', 'serviceusage.googleapis.com') | ForEach-Object {
  $imports["google_project_service.required[`"$_`"]"] = "$project/$_"
}
@('int2-api-key', 'int2-app-password', 'int2-db-password', 'int2-db-user', 'int2-gemini-key', 'int2-google-maps-key', 'int2-session-token') | ForEach-Object {
  $imports["google_secret_manager_secret.app[`"$_`"]"] = "projects/$project/secrets/$_"
}

foreach ($entry in $imports.GetEnumerator()) {
  terraform import $entry.Key $entry.Value
  if ($LASTEXITCODE -ne 0) { throw "Import failed: $($entry.Key)" }
}
Write-Host "Imported existing INT2 resources. Run 'terraform plan' next."
