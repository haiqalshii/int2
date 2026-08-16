resource "google_service_account" "runtime" {
  account_id   = "int2-cloud-run"
  display_name = "INT2 Cloud Run runtime"
  description  = "Least-privilege runtime identity for INT2"
  depends_on   = [google_project_service.required]
}

resource "google_service_account" "build" {
  count        = var.enable_github_triggers ? 1 : 0
  account_id   = "int2-cloud-build"
  display_name = "INT2 Cloud Build deployer"
  description  = "Builds images and deploys INT2 Cloud Run revisions"
  depends_on   = [google_project_service.required]
}

resource "google_project_iam_member" "cloud_build_run_admin" {
  count   = var.enable_github_triggers ? 1 : 0
  project = var.project_id
  role    = "roles/run.admin"
  member  = "serviceAccount:${google_service_account.build[0].email}"
}

resource "google_project_iam_member" "cloud_build_service_account_user" {
  count   = var.enable_github_triggers ? 1 : 0
  project = var.project_id
  role    = "roles/iam.serviceAccountUser"
  member  = "serviceAccount:${google_service_account.build[0].email}"
}

resource "google_project_iam_member" "cloud_build_log_writer" {
  count   = var.enable_github_triggers ? 1 : 0
  project = var.project_id
  role    = "roles/logging.logWriter"
  member  = "serviceAccount:${google_service_account.build[0].email}"
}

resource "google_artifact_registry_repository_iam_member" "cloud_build_writer" {
  count      = var.enable_github_triggers ? 1 : 0
  project    = var.project_id
  location   = var.region
  repository = google_artifact_registry_repository.containers.repository_id
  role       = "roles/artifactregistry.writer"
  member     = "serviceAccount:${google_service_account.build[0].email}"
}
