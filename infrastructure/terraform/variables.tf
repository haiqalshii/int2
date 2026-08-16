variable "project_id" {
  type    = string
  default = "project-int2"
}
variable "region" {
  type    = string
  default = "asia-southeast1"
}
variable "backend_service_name" {
  type    = string
  default = "int2-backend"
}
variable "frontend_service_name" {
  type    = string
  default = "int2-frontend"
}
variable "artifact_repository" {
  type    = string
  default = "cloud-run-source-deploy"
}
variable "backend_image_tag" {
  type    = string
  default = "latest"
}
variable "frontend_image_tag" {
  type    = string
  default = "latest"
}
variable "github_owner" {
  type     = string
  default  = null
  nullable = true
}
variable "github_repository" {
  type     = string
  default  = null
  nullable = true
}
variable "github_branch_regex" {
  type    = string
  default = "^main$"
}
variable "enable_github_triggers" {
  type    = bool
  default = false
}
