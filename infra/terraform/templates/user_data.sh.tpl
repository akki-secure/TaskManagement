#!/bin/bash
# このスクリプトはEC2インスタンス起動時に自動実行される
# ログは /var/log/user-data.log で確認できる
exec > >(tee /var/log/user-data.log | logger -t user-data -s 2>/dev/console) 2>&1

echo "=== バックエンドセットアップ開始 ==="

# パッケージを最新化してDockerをインストール
dnf update -y
dnf install -y docker

# Dockerを起動して自動起動設定
systemctl start docker
systemctl enable docker
usermod -aG docker ec2-user

echo "=== ECRにログイン ==="
aws ecr get-login-password --region ${aws_region} | \
  docker login --username AWS --password-stdin ${ecr_repository_url}

echo "=== バックエンドコンテナを起動 ==="
docker run -d \
  --name backend \
  --restart always \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=production \
  -e SPRING_DATASOURCE_URL="jdbc:postgresql://${db_endpoint}/${db_name}" \
  -e SPRING_DATASOURCE_USERNAME="${db_username}" \
  -e SPRING_DATASOURCE_PASSWORD="${db_password}" \
  ${ecr_repository_url}:${backend_image_tag}

echo "=== セットアップ完了 ==="
