output "frontend_url" { value = google_cloud_run_v2_service.frontend.uri }
output "backend_url" { value = google_cloud_run_v2_service.backend.uri }
output "runtime_service_account" { value = google_service_account.runtime.email }
output "artifact_repository_url" { value = local.artifact_base }
output "secret_ids" { value = sort(tolist(local.secret_ids)) }
output "database_connection_name" { value = google_sql_database_instance.postgres.connection_name }
output "database_name" { value = google_sql_database.app.name }
output "candidate_bucket" { value = google_storage_bucket.candidate_files.name }
