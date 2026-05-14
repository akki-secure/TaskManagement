variable "aws_region" {
  description = "AWSリージョン"
  type        = string
  default     = "ap-northeast-1"
}

variable "project_name" {
  description = "プロジェクト名（リソース名のプレフィックスに使用）"
  type        = string
  default     = "taskmanagement"
}

variable "environment" {
  description = "環境名"
  type        = string
  default     = "production"
}

variable "db_password" {
  description = "PostgreSQLのパスワード（terraform.tfvarsで設定。Gitにコミットしない）"
  type        = string
  sensitive   = true
}

variable "db_username" {
  description = "PostgreSQLのユーザー名"
  type        = string
  default     = "postgres"
}

variable "db_name" {
  description = "データベース名"
  type        = string
  default     = "taskmanagement"
}

variable "backend_image_tag" {
  description = "バックエンドDockerイメージのタグ"
  type        = string
  default     = "latest"
}
