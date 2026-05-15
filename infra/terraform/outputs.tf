# デプロイ後に表示される重要な情報
output "frontend_url" {
  description = "フロントエンドのURL（CloudFront）"
  value       = "https://${aws_cloudfront_distribution.frontend.domain_name}"
}

output "backend_url" {
  description = "バックエンドAPIのURL（EC2のパブリックIP）"
  value       = "http://${aws_instance.backend.public_ip}:8080"
}

output "backend_public_ip" {
  description = "EC2のパブリックIPアドレス（SSH接続に使用）"
  value       = aws_instance.backend.public_ip
}

output "ssh_command" {
  description = "EC2へのSSH接続コマンド（key_pair_nameを設定した場合）"
  value       = "ssh -i ~/.ssh/${var.key_pair_name}.pem ec2-user@${aws_instance.backend.public_ip}"
}

output "ecr_repository_url" {
  description = "ECRリポジトリURL（Dockerイメージのプッシュ先）"
  value       = aws_ecr_repository.backend.repository_url
}

output "rds_endpoint" {
  description = "RDSエンドポイント（アプリからの接続先）"
  value       = aws_db_instance.postgres.endpoint
  sensitive   = true
}

output "s3_bucket_name" {
  description = "フロントエンドのS3バケット名"
  value       = aws_s3_bucket.frontend.bucket
}

output "cloudfront_distribution_id" {
  description = "CloudFrontディストリビューションID（キャッシュ削除に使用）"
  value       = aws_cloudfront_distribution.frontend.id
}
