# RDS サブネットグループ（どのサブネットにDBを置くか指定）
resource "aws_db_subnet_group" "main" {
  name       = "${var.project_name}-db-subnet-group"
  subnet_ids = aws_subnet.private[*].id

  tags = {
    Name = "${var.project_name}-db-subnet-group"
  }
}

# RDS PostgreSQL インスタンス
resource "aws_db_instance" "postgres" {
  identifier = "${var.project_name}-postgres"

  engine         = "postgres"
  engine_version = "16.3"
  instance_class = "db.t3.micro" # 無料枠対象（750時間/月）

  allocated_storage     = 20  # GB
  max_allocated_storage = 100 # オートスケーリング上限

  db_name  = var.db_name
  username = var.db_username
  password = var.db_password

  db_subnet_group_name   = aws_db_subnet_group.main.name
  vpc_security_group_ids = [aws_security_group.rds.id]

  # 本番環境では true にすること
  skip_final_snapshot = true

  # 自動バックアップ（7日間保持）
  backup_retention_period = 7
  backup_window           = "03:00-04:00" # 日本時間 午後12時〜1時

  # メンテナンスウィンドウ
  maintenance_window = "mon:04:00-mon:05:00"

  # パフォーマンスインサイト（無効 = 無料）
  performance_insights_enabled = false

  tags = {
    Name        = "${var.project_name}-postgres"
    Environment = var.environment
  }
}
