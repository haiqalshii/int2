resource "google_artifact_registry_repository" "containers" {
  location      = var.region
  repository_id = var.artifact_repository
  description   = "INT2 Cloud Run container images"
  format        = "DOCKER"
  depends_on    = [google_project_service.required]
}
