#!/bin/bash
# フロントエンドをビルドしてEC2にデプロイするスクリプト
# 使い方: bash infra/scripts/deploy-frontend.sh
#
# 前提条件:
#   - terraform apply 済みで EC2 が起動していること
#   - KEY_PATH 環境変数またはデフォルト (~/.ssh/taskmanagement.pem) にキーペアがあること
#
# 例:
#   KEY_PATH=~/.ssh/my-key.pem bash infra/scripts/deploy-frontend.sh

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
KEY_PATH="${KEY_PATH:-$HOME/.ssh/taskmanagement.pem}"
TERRAFORM_DIR="$REPO_ROOT/infra/terraform"
FRONTEND_DIR="$REPO_ROOT/frontend"

echo "=== EC2のIPアドレスを取得 ==="
EC2_IP=$(terraform -chdir="$TERRAFORM_DIR" output -raw ec2_public_ip)
echo "EC2 IP: $EC2_IP"

echo "=== フロントエンドをビルド ==="
cd "$FRONTEND_DIR"
npm run build

echo "=== ビルド済みファイルをEC2に転送 ==="
scp -i "$KEY_PATH" \
    -o StrictHostKeyChecking=no \
    -r dist/* \
    "ec2-user@$EC2_IP:/usr/share/nginx/html/"

echo "=== nginxを再読み込み ==="
ssh -i "$KEY_PATH" \
    -o StrictHostKeyChecking=no \
    "ec2-user@$EC2_IP" \
    "sudo nginx -t && sudo systemctl reload nginx"

echo ""
echo "デプロイ完了: http://$EC2_IP"
