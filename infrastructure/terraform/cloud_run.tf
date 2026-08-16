resource "google_cloud_run_v2_service" "backend" {
  name                = var.backend_service_name
  location            = var.region
  deletion_protection = true
  ingress             = "INGRESS_TRAFFIC_ALL"
  template {
    service_account                  = google_service_account.runtime.email
    timeout                          = "300s"
    max_instance_request_concurrency = 80
    scaling {
      min_instance_count = 0
      max_instance_count = 2
    }
    containers {
      image = local.backend_image
      ports { container_port = 8080 }
      resources {
        limits            = { cpu = "1", memory = "512Mi" }
        startup_cpu_boost = true
      }
      startup_probe {
        timeout_seconds   = 240
        period_seconds    = 240
        failure_threshold = 1
        tcp_socket { port = 8080 }
      }
      dynamic "env" {
        for_each = {
          LLM_PROVIDER              = "gemini"
          GEMINI_MODEL              = "gemini-2.5-flash"
          LLM_TIMEOUT_SECONDS       = "45"
          SUPABASE_CANDIDATE_BUCKET = "candidate-files"
          FRONTEND_ORIGIN           = local.frontend_url
        }
        content {
          name  = env.key
          value = env.value
        }
      }
      dynamic "env" {
        for_each = {
          SPRING_DATASOURCE_URL      = "int2-db-url"
          SPRING_DATASOURCE_USERNAME = "int2-db-user"
          SPRING_DATASOURCE_PASSWORD = "int2-db-password"
          SUPABASE_URL               = "int2-supabase-url"
          SUPABASE_SERVICE_KEY       = "int2-supabase-service-key"
          GEMINI_API_KEY             = "int2-gemini-key"
          APP_API_KEY                = "int2-api-key"
        }
        content {
          name = env.key
          value_source {
            secret_key_ref {
              secret  = env.value
              version = "latest"
            }
          }
        }
      }
    }
  }
  lifecycle { ignore_changes = [template[0].containers[0].image] }
  depends_on = [google_secret_manager_secret_iam_member.runtime_access]
}

resource "google_cloud_run_v2_service" "frontend" {
  name                = var.frontend_service_name
  location            = var.region
  deletion_protection = true
  ingress             = "INGRESS_TRAFFIC_ALL"
  template {
    service_account                  = google_service_account.runtime.email
    timeout                          = "300s"
    max_instance_request_concurrency = 80
    scaling {
      min_instance_count = 0
      max_instance_count = 2
    }
    containers {
      image = local.frontend_image
      ports { container_port = 3000 }
      resources {
        limits            = { cpu = "1", memory = "512Mi" }
        startup_cpu_boost = true
      }
      startup_probe {
        timeout_seconds   = 240
        period_seconds    = 240
        failure_threshold = 1
        tcp_socket { port = 3000 }
      }
      env {
        name  = "BACKEND_URL"
        value = local.backend_url
      }
      dynamic "env" {
        for_each = {
          APP_PASSWORD            = "int2-app-password"
          APP_SESSION_TOKEN       = "int2-session-token"
          APP_API_KEY             = "int2-api-key"
          GOOGLE_MAPS_BROWSER_KEY = "int2-google-maps-key"
        }
        content {
          name = env.key
          value_source {
            secret_key_ref {
              secret  = env.value
              version = "latest"
            }
          }
        }
      }
    }
  }
  lifecycle { ignore_changes = [template[0].containers[0].image] }
  depends_on = [google_secret_manager_secret_iam_member.runtime_access]
}

resource "google_cloud_run_v2_service_iam_member" "backend_public" {
  project  = var.project_id
  location = var.region
  name     = google_cloud_run_v2_service.backend.name
  role     = "roles/run.invoker"
  member   = "allUsers"
}

resource "google_cloud_run_v2_service_iam_member" "frontend_public" {
  project  = var.project_id
  location = var.region
  name     = google_cloud_run_v2_service.frontend.name
  role     = "roles/run.invoker"
  member   = "allUsers"
}
