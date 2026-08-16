locals {
  monitoring_notification_channels = var.monitoring_alert_email == null ? [] : [google_monitoring_notification_channel.email[0].name]
}

resource "google_monitoring_notification_channel" "email" {
  count        = var.monitoring_alert_email == null ? 0 : 1
  display_name = "INT2 operations email"
  type         = "email"
  labels = {
    email_address = var.monitoring_alert_email
  }
  depends_on = [google_project_service.required]
}

resource "google_monitoring_uptime_check_config" "backend" {
  display_name       = "INT2 backend health"
  timeout            = "10s"
  period             = "60s"
  log_check_failures = true

  http_check {
    path           = "/api/health"
    port           = 443
    request_method = "GET"
    use_ssl        = true
    validate_ssl   = true
  }

  monitored_resource {
    type = "uptime_url"
    labels = {
      host       = trimprefix(local.backend_url, "https://")
      project_id = var.project_id
    }
  }

  content_matchers {
    content = "\"status\":\"ok\""
  }

  depends_on = [google_project_service.required, google_cloud_run_v2_service.backend]
}

resource "google_monitoring_alert_policy" "backend_down" {
  display_name          = "INT2 backend unavailable"
  combiner              = "OR"
  notification_channels = local.monitoring_notification_channels

  conditions {
    display_name = "Health check failed"
    condition_threshold {
      filter          = "metric.type=\"monitoring.googleapis.com/uptime_check/check_passed\" AND resource.type=\"uptime_url\" AND metric.label.check_id=\"${google_monitoring_uptime_check_config.backend.uptime_check_id}\""
      comparison      = "COMPARISON_LT"
      threshold_value = 1
      duration        = "120s"
      aggregations {
        alignment_period   = "60s"
        per_series_aligner = "ALIGN_NEXT_OLDER"
      }
      trigger { count = 1 }
    }
  }

  alert_strategy {
    auto_close = "1800s"
  }
  documentation {
    content   = "The public backend health endpoint has failed for at least two minutes. Check the latest int2-backend Cloud Run revision logs."
    mime_type = "text/markdown"
  }
}

resource "google_monitoring_alert_policy" "backend_5xx" {
  display_name          = "INT2 backend elevated 5xx errors"
  combiner              = "OR"
  notification_channels = local.monitoring_notification_channels

  conditions {
    display_name = "Backend returned 5xx responses"
    condition_threshold {
      filter          = "metric.type=\"run.googleapis.com/request_count\" AND resource.type=\"cloud_run_revision\" AND resource.label.service_name=\"${var.backend_service_name}\" AND metric.label.response_code_class=\"5xx\""
      comparison      = "COMPARISON_GT"
      threshold_value = 0
      duration        = "60s"
      aggregations {
        alignment_period     = "60s"
        per_series_aligner   = "ALIGN_RATE"
        cross_series_reducer = "REDUCE_SUM"
        group_by_fields      = ["resource.label.service_name"]
      }
    }
  }
  documentation {
    content   = "The backend returned one or more server errors. Inspect int2-backend request and application logs."
    mime_type = "text/markdown"
  }
}

resource "google_monitoring_alert_policy" "database_cpu" {
  display_name          = "INT2 database high CPU"
  combiner              = "OR"
  notification_channels = local.monitoring_notification_channels

  conditions {
    display_name = "Cloud SQL CPU above 80 percent"
    condition_threshold {
      filter          = "metric.type=\"cloudsql.googleapis.com/database/cpu/utilization\" AND resource.type=\"cloudsql_database\" AND resource.label.database_id=\"${var.project_id}:${google_sql_database_instance.postgres.name}\""
      comparison      = "COMPARISON_GT"
      threshold_value = 0.8
      duration        = "300s"
      aggregations {
        alignment_period   = "300s"
        per_series_aligner = "ALIGN_MEAN"
      }
    }
  }
  documentation {
    content   = "Cloud SQL CPU has remained above 80 percent for five minutes. Review queries and instance sizing."
    mime_type = "text/markdown"
  }
}
