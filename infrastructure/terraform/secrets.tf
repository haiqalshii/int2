resource "google_secret_manager_secret" "app" {
  for_each  = local.secret_ids
  secret_id = each.value
  replication {
    auto {}
  }
  depends_on = [google_project_service.required]
}

# Payload versions stay outside Terraform to keep plaintext out of state.
resource "google_secret_manager_secret_iam_member" "runtime_access" {
  for_each  = local.secret_ids
  project   = var.project_id
  secret_id = google_secret_manager_secret.app[each.key].secret_id
  role      = "roles/secretmanager.secretAccessor"
  member    = "serviceAccount:${google_service_account.runtime.email}"
}
