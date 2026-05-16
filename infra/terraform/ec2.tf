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
# t3.micro = 無料枠対象（750時間/月、新規アカウントから12ヶ月間）
resource "aws_instance" "main" {
  ami                         = data.aws_ami.amazon_linux_2023.id
  instance_type               = "t3.micro"
  subnet_id                   = aws_subnet.public.id
  vpc_security_group_ids      = [aws_security_group.ec2.id]
  associate_public_ip_address = true

  key_name = var.key_pair_name

  # user_data = インスタンス起動時に自動実行されるスクリプト
  # 注意: <<EOF（インデントなし）を使うこと。<<-EOF はタブのみ除去するため
  #       スペースインデントがあると #!/bin/bash の前に空白が入り cloud-init が実行しない。
  user_data = <<EOF
#!/bin/bash
dnf update -y

# Java 21（Spring Boot バックエンド用）
dnf install -y java-21-amazon-corretto

# nginx（フロントエンドの静的ファイル配信用）
dnf install -y nginx

# デフォルトのサーバーブロック（server_name _ on port 80）を削除してapp.confと競合しないようにする
sed -i '/^    server {/,/^    }$/d' /etc/nginx/nginx.conf

# nginx 設定: /api → Spring Boot リバースプロキシ + React Router SPAフォールバック
cat > /etc/nginx/conf.d/app.conf << 'NGINXEOF'
server {
    listen 80;
    server_name _;
    root /usr/share/nginx/html;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass         http://localhost:8080;
        proxy_set_header   Host $host;
        proxy_set_header   X-Real-IP $remote_addr;
        proxy_set_header   X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
NGINXEOF

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
