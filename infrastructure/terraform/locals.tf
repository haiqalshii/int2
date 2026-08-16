data "google_project" "current" { project_id = var.project_id }

locals {
  artifact_base         = "${var.region}-docker.pkg.dev/${var.project_id}/${var.artifact_repository}"
  backend_image         = "${local.artifact_base}/${var.backend_service_name}:${var.backend_image_tag}"
  frontend_image        = "${local.artifact_base}/${var.frontend_service_name}:${var.frontend_image_tag}"
  backend_url           = "https://${var.backend_service_name}-${data.google_project.current.number}.${var.region}.run.app"
  frontend_url          = "https://${var.frontend_service_name}-${data.google_project.current.number}.${var.region}.run.app"
  candidate_bucket_name = coalesce(var.candidate_bucket_name, "${var.project_id}-int2-candidate-files")
  database_url          = "jdbc:postgresql:///${var.database_name}?cloudSqlInstance=${google_sql_database_instance.postgres.connection_name}&socketFactory=com.google.cloud.sql.postgres.SocketFactory&ipTypes=PUBLIC,PRIVATE"
  secret_ids = toset([
    "int2-api-key", "int2-app-password", "int2-db-password",
    "int2-db-user", "int2-gemini-key", "int2-google-maps-key",
    "int2-session-token"
  ])
  required_apis = toset([
    "artifactregistry.googleapis.com", "cloudbuild.googleapis.com",
    "iam.googleapis.com", "run.googleapis.com", "sqladmin.googleapis.com",
    "storage.googleapis.com",
    "secretmanager.googleapis.com", "serviceusage.googleapis.com"
  ])
}
