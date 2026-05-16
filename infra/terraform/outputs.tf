# terraform apply 後に表示される情報
output "ec2_public_ip" {
  description = "EC2のパブリックIPアドレス"
  value       = aws_instance.main.public_ip
}

output "frontend_url" {
  description = "フロントエンドのURL（nginx）"
  value       = "http://${aws_instance.main.public_ip}"
}

output "backend_url" {
  description = "バックエンドAPIのURL（Spring Boot）"
  value       = "http://${aws_instance.main.public_ip}:8080"
}

output "ssh_command" {
  description = "SSH接続コマンド（キーペアを設定した場合）"
  value       = "ssh -i ~/.ssh/<キーペア名>.pem ec2-user@${aws_instance.main.public_ip}"
}

output "rds_endpoint" {
  description = "RDSのエンドポイント（EC2内からのみ接続可能）"
  value       = aws_db_instance.main.endpoint
}

output "rds_db_name" {
  description = "RDSのデータベース名"
  value       = aws_db_instance.main.db_name
}
