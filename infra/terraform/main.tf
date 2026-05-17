terraform {
  required_version = ">= 1.5.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }

  # デプロイ状態をS3に保存（ローカル消失・チーム開発・CI/CD対応）
  # バケット名は terraform.tfvars.example を参照して設定すること
  # terraform init -backend-config="bucket=<your-bucket-name>" で上書きも可能
  backend "s3" {
    bucket = "REPLACE_WITH_YOUR_BUCKET_NAME"
    key    = "terraform.tfstate"
    region = "ap-northeast-1"
  }
}

provider "aws" {
  region = var.aws_region
}
