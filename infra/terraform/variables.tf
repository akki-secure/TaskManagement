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

variable "key_pair_name" {
  description = "EC2へのSSH接続に使うキーペア名。AWSコンソール → EC2 → キーペア で事前に作成しておく。"
  type        = string
}

variable "allowed_ip" {
  description = "セキュリティグループで許可するIPアドレス（CIDR形式）。curl https://checkip.amazonaws.com で確認。"
  type        = string
}
