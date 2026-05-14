#!/bin/bash
# TaskManagement AWSデプロイスクリプト
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
echo "[1/6] 前提ツールの確認..."
command -v aws >/dev/null 2>&1 || { echo "ERROR: aws CLI がインストールされていません"; exit 1; }
command -v terraform >/dev/null 2>&1 || { echo "ERROR: terraform がインストールされていません"; exit 1; }
command -v docker >/dev/null 2>&1 || { echo "ERROR: docker がインストールされていません"; exit 1; }

AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
echo "  AWS アカウントID: $AWS_ACCOUNT_ID"
echo "  リージョン: $AWS_REGION"

# ===== Terraform インフラ作成 =====
echo ""
echo "[2/6] Terraform でインフラを構築..."
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
BACKEND_URL=$(terraform output -raw backend_url)
FRONTEND_URL=$(terraform output -raw frontend_url)

# ===== バックエンド Dockerイメージのビルド & プッシュ =====
echo ""
echo "[3/6] バックエンド Dockerイメージをビルド..."
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

# ===== ECSサービスを再起動（新イメージを反映）=====
echo ""
echo "[4/6] ECSサービスを更新..."
aws ecs update-service \
  --cluster "${PROJECT_NAME}-cluster" \
  --service "${PROJECT_NAME}-backend" \
  --force-new-deployment \
  --region "$AWS_REGION" \
  --query 'service.serviceName' \
  --output text

echo "  ECSデプロイを開始しました（完了まで数分かかります）"

# ===== フロントエンドをビルド & S3にアップロード =====
echo ""
echo "[5/6] フロントエンドをビルド & S3にアップロード..."
cd "$ROOT_DIR/frontend"

# 本番環境用の環境変数を設定
echo "VITE_API_BASE_URL=$BACKEND_URL" > .env.production

npm ci
npm run build

# S3にアップロード
aws s3 sync dist/ "s3://$S3_BUCKET/" --delete
echo "  S3にアップロード完了"

# ===== CloudFrontキャッシュを削除 =====
echo ""
echo "[6/6] CloudFrontキャッシュをクリア..."
INVALIDATION_ID=$(aws cloudfront create-invalidation \
  --distribution-id "$CLOUDFRONT_ID" \
  --paths "/*" \
  --query 'Invalidation.Id' \
  --output text)
echo "  キャッシュ削除リクエスト: $INVALIDATION_ID"

# ===== 完了 =====
echo ""
echo "========================================="
echo "  デプロイ完了！"
echo "========================================="
echo ""
echo "  フロントエンド: $FRONTEND_URL"
echo "  バックエンドAPI: $BACKEND_URL"
echo ""
echo "※ ECSの起動に数分かかる場合があります"
echo "※ CloudFrontのキャッシュ削除に最大15分かかる場合があります"
