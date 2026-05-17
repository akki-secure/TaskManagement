terraform {
  required_version = ">= 1.5.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }

  # デプロイ状態をS3に保存（ローカル消失・チーム開発・CI/CD対応）
  backend "s3" {
    bucket = "taskmanagement-tfstate-174516979085"
    key    = "terraform.tfstate"
    region = "ap-northeast-1"
  }
}

provider "aws" {
  region = var.aws_region
}
