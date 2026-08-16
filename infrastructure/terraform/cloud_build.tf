locals {
  create_triggers = var.enable_github_triggers && var.github_owner != null && var.github_repository != null
}

resource "google_cloudbuild_trigger" "backend" {
  count           = local.create_triggers ? 1 : 0
  name            = "int2-backend-main"
  location        = var.region
  service_account = google_service_account.build[0].id
  github {
    owner = var.github_owner
    name  = var.github_repository
    push { branch = var.github_branch_regex }
  }
  included_files = ["backend/**"]
  build {
    step {
      name = "gcr.io/cloud-builders/docker"
      args = ["build", "-t", "${local.artifact_base}/${var.backend_service_name}:$COMMIT_SHA", "backend"]
    }
    step {
      name       = "gcr.io/google.com/cloudsdktool/cloud-sdk:slim"
      entrypoint = "gcloud"
      args       = ["run", "services", "update", var.backend_service_name, "--image", "${local.artifact_base}/${var.backend_service_name}:$COMMIT_SHA", "--region", var.region, "--quiet"]
    }
    images = ["${local.artifact_base}/${var.backend_service_name}:$COMMIT_SHA"]
    options { logging = "CLOUD_LOGGING_ONLY" }
  }
  depends_on = [google_project_iam_member.cloud_build_run_admin]
}

resource "google_cloudbuild_trigger" "frontend" {
  count           = local.create_triggers ? 1 : 0
  name            = "int2-frontend-main"
  location        = var.region
  service_account = google_service_account.build[0].id
  github {
    owner = var.github_owner
    name  = var.github_repository
    push { branch = var.github_branch_regex }
  }
  included_files = ["frontend/**"]
  build {
    step {
      name = "gcr.io/cloud-builders/docker"
      args = ["build", "-t", "${local.artifact_base}/${var.frontend_service_name}:$COMMIT_SHA", "frontend"]
    }
    step {
      name       = "gcr.io/google.com/cloudsdktool/cloud-sdk:slim"
      entrypoint = "gcloud"
      args       = ["run", "services", "update", var.frontend_service_name, "--image", "${local.artifact_base}/${var.frontend_service_name}:$COMMIT_SHA", "--region", var.region, "--quiet"]
    }
    images = ["${local.artifact_base}/${var.frontend_service_name}:$COMMIT_SHA"]
    options { logging = "CLOUD_LOGGING_ONLY" }
  }
  depends_on = [google_project_iam_member.cloud_build_run_admin]
}
