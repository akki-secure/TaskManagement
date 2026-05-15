# Amazon Linux 2023 の最新AMIを自動取得
# AMI = EC2インスタンスの「OS入りの雛形」
data "aws_ami" "amazon_linux_2023" {
  most_recent = true
  owners      = ["amazon"]

  filter {
    name   = "name"
    values = ["al2023-ami-*-x86_64"]
  }

  filter {
    name   = "virtualization-type"
    values = ["hvm"]
  }
}

# EC2インスタンス（バックエンドのSpring Bootを動かすサーバー）
# t2.micro = 無料枠対象（750時間/月、12ヶ月間）
resource "aws_instance" "backend" {
  ami                    = data.aws_ami.amazon_linux_2023.id
  instance_type          = "t2.micro"
  subnet_id              = aws_subnet.public[0].id
  vpc_security_group_ids = [aws_security_group.ec2.id]
  iam_instance_profile   = aws_iam_instance_profile.ec2.name
  key_name               = var.key_pair_name

  # パブリックIPを自動付与（インターネットからアクセスするため）
  associate_public_ip_address = true

  # user_data = インスタンス起動時に自動実行されるスクリプト
  user_data = base64encode(templatefile("${path.module}/templates/user_data.sh.tpl", {
    aws_region          = var.aws_region
    ecr_repository_url  = aws_ecr_repository.backend.repository_url
    backend_image_tag   = var.backend_image_tag
    db_endpoint         = aws_db_instance.postgres.endpoint
    db_name             = var.db_name
    db_username         = var.db_username
    db_password         = var.db_password
  }))

  # ルートボリューム（OS用のディスク）
  root_block_device {
    volume_type = "gp3"
    volume_size = 20  # GB（無料枠: 30GBまで）
  }

  tags = {
    Name        = "${var.project_name}-backend"
    Environment = var.environment
  }
}

# CloudWatch ロググループ（アプリのログを保存する場所）
# 7日保持（個人開発なので短めに設定してコスト削減）
resource "aws_cloudwatch_log_group" "backend" {
  name              = "/ec2/${var.project_name}/backend"
  retention_in_days = 7

  tags = {
    Name = "${var.project_name}-backend-logs"
  }
}
