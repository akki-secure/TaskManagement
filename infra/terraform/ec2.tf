# Amazon Linux 2023 の最新AMIを自動取得
# AMI = EC2インスタンスの「OSが入った雛形イメージ」
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

# EC2インスタンス = AWSのサーバー
# t2.micro = 無料枠対象（750時間/月、新規アカウントから12ヶ月間）
resource "aws_instance" "main" {
  ami                         = data.aws_ami.amazon_linux_2023.id
  instance_type               = "t2.micro"
  subnet_id                   = aws_subnet.public.id
  vpc_security_group_ids      = [aws_security_group.ec2.id]
  associate_public_ip_address = true

  # key_name = var.key_pair_name
  # ↑ キーペアでSSH接続する場合はコメントを外す
  # AWSコンソール → EC2 → キーペア で事前に作成が必要

  # user_data = インスタンス起動時に自動実行されるスクリプト
  # Java 21 と nginx をインストールしておく
  user_data = <<-EOF
    #!/bin/bash
    dnf update -y

    # Java 21（Spring Boot バックエンド用）
    dnf install -y java-21-amazon-corretto

    # nginx（フロントエンドの静的ファイル配信用）
    dnf install -y nginx
    systemctl enable nginx
    systemctl start nginx
  EOF

  # ルートボリューム（OS用のディスク）
  root_block_device {
    volume_type = "gp3"
    volume_size = 20  # GB（無料枠: 30GBまで）
  }

  tags = {
    Name = "${var.project_name}-server"
  }
}
