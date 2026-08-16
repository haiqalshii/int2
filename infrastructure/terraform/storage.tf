resource "google_storage_bucket" "candidate_files" {
  name                        = local.candidate_bucket_name
  location                    = var.region
  uniform_bucket_level_access = true
  force_destroy               = false

  public_access_prevention = "enforced"

  versioning {
    enabled = true
  }

  depends_on = [google_project_service.required]
}

resource "google_storage_bucket_iam_member" "runtime_candidate_files" {
  bucket = google_storage_bucket.candidate_files.name
  role   = "roles/storage.objectAdmin"
  member = "serviceAccount:${google_service_account.runtime.email}"
}

resource "google_project_iam_member" "runtime_cloud_sql" {
  project = var.project_id
  role    = "roles/cloudsql.client"
  member  = "serviceAccount:${google_service_account.runtime.email}"
}
