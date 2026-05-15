terraform {
  required_version = ">= 1.5.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }

  # デプロイ状態をS3に保存（チーム開発やCI/CDに必要）
  # 初回はコメントアウトして、後で有効化する
  # backend "s3" {
  #   bucket = "taskmanagement-tfstate-<あなたのAWSアカウントID>"
  #   key    = "terraform.tfstate"
  #   region = "ap-northeast-1"
  # }
}

provider "aws" {
  region = var.aws_region
}
