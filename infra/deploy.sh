#!/bin/bash
# TaskManagement AWSデプロイスクリプト（無料枠構成: EC2 + RDS + S3 + CloudFront）
# 使い方: ./infra/deploy.sh
set -e

# ===== 設定 =====
AWS_REGION="ap-northeast-1"
PROJECT_NAME="taskmanagement"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(dirname "$SCRIPT_DIR")"

echo "========================================="
echo "  TaskManagement AWS デプロイ"
echo "========================================="

# ===== 前提チェック =====
echo ""
echo "[1/5] 前提ツールの確認..."
command -v aws >/dev/null 2>&1 || { echo "ERROR: aws CLI がインストールされていません"; exit 1; }
command -v terraform >/dev/null 2>&1 || { echo "ERROR: terraform がインストールされていません"; exit 1; }
command -v docker >/dev/null 2>&1 || { echo "ERROR: docker がインストールされていません"; exit 1; }

AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
echo "  AWS アカウントID: $AWS_ACCOUNT_ID"
echo "  リージョン: $AWS_REGION"

# ===== Terraform インフラ作成 =====
echo ""
echo "[2/5] Terraform でインフラを構築..."
cd "$SCRIPT_DIR/terraform"

if [ ! -f "terraform.tfvars" ]; then
  echo "ERROR: terraform/terraform.tfvars が見つかりません"
  echo "  terraform.tfvars.example をコピーして設定してください:"
  echo "  cp terraform.tfvars.example terraform.tfvars"
  exit 1
fi

terraform init
terraform plan -out=tfplan
echo ""
echo "上記の変更内容を確認してください。続行しますか？ (yes/no)"
read -r CONFIRM
if [ "$CONFIRM" != "yes" ]; then
  echo "デプロイをキャンセルしました"
  exit 0
fi

terraform apply tfplan

# ===== Terraformの出力を取得 =====
ECR_URL=$(terraform output -raw ecr_repository_url)
S3_BUCKET=$(terraform output -raw s3_bucket_name)
CLOUDFRONT_ID=$(terraform output -raw cloudfront_distribution_id)
BACKEND_IP=$(terraform output -raw backend_public_ip)
BACKEND_URL=$(terraform output -raw backend_url)
FRONTEND_URL=$(terraform output -raw frontend_url)

# ===== バックエンド Dockerイメージのビルド & プッシュ =====
echo ""
echo "[3/5] バックエンド Dockerイメージをビルド..."
cd "$ROOT_DIR/backend"

# ECRにログイン
aws ecr get-login-password --region "$AWS_REGION" | \
  docker login --username AWS --password-stdin "$ECR_URL"

# イメージをビルド
docker build --platform linux/amd64 -t "$PROJECT_NAME-backend" .

# ECRにプッシュ
docker tag "$PROJECT_NAME-backend:latest" "$ECR_URL:latest"
docker push "$ECR_URL:latest"
echo "  完了: $ECR_URL:latest"

# ===== EC2でコンテナを再起動（新イメージを反映）=====
echo ""
echo "[4/5] EC2でバックエンドコンテナを更新..."
echo "  EC2 IP: $BACKEND_IP"
echo ""
echo "  ※ EC2に直接ログインして以下のコマンドを実行してください:"
echo ""
echo "  ssh ec2-user@$BACKEND_IP"
echo "  aws ecr get-login-password --region $AWS_REGION | \\"
echo "    docker login --username AWS --password-stdin $ECR_URL"
echo "  docker pull $ECR_URL:latest"
echo "  docker stop backend && docker rm backend"
echo "  docker run -d --name backend --restart always -p 8080:8080 \\"
echo "    -e SPRING_PROFILES_ACTIVE=production \\"
echo "    -e SPRING_DATASOURCE_URL=jdbc:postgresql://... \\"
echo "    $ECR_URL:latest"
echo ""
echo "  ※ SSMを使う場合はAWSコンソール → EC2 → 接続 → Session Manager"

# ===== フロントエンドをビルド & S3にアップロード =====
echo ""
echo "[5/5] フロントエンドをビルド & S3にアップロード..."
cd "$ROOT_DIR/frontend"

# 本番環境用の環境変数を設定
echo "VITE_API_BASE_URL=$BACKEND_URL" > .env.production

npm ci
npm run build

# S3にアップロード
aws s3 sync dist/ "s3://$S3_BUCKET/" --delete
echo "  S3にアップロード完了"

# CloudFrontキャッシュを削除
INVALIDATION_ID=$(aws cloudfront create-invalidation \
  --distribution-id "$CLOUDFRONT_ID" \
  --paths "/*" \
  --query 'Invalidation.Id' \
  --output text)
echo "  CloudFrontキャッシュ削除リクエスト: $INVALIDATION_ID"

# ===== 完了 =====
echo ""
echo "========================================="
echo "  デプロイ完了！"
echo "========================================="
echo ""
echo "  フロントエンド: $FRONTEND_URL"
echo "  バックエンドAPI: $BACKEND_URL"
echo "  EC2 SSH: ssh ec2-user@$BACKEND_IP"
echo ""
echo "※ CloudFrontのキャッシュ削除に最大15分かかる場合があります"
