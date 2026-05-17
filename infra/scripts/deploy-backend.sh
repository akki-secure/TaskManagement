#!/bin/bash
# バックエンドをビルドしてEC2にデプロイするスクリプト
# 使い方: bash infra/scripts/deploy-backend.sh
#
# 前提条件:
#   - terraform apply 済みで EC2・RDS が起動していること
#   - KEY_PATH 環境変数またはデフォルト (~/.ssh/taskmanagement.pem) にキーペアがあること
#   - 以下の環境変数が設定されていること:
#       DB_PASSWORD  : RDSのパスワード（terraform.tfvarsのdb_passwordと同じ値）
#       JWT_SECRET   : 本番用JWTシークレット（32文字以上のランダム文字列）
#
# 例:
#   export DB_PASSWORD="your-db-password"
#   export JWT_SECRET="your-32-char-or-longer-jwt-secret"
#   bash infra/scripts/deploy-backend.sh
#   # SSHキーを明示する場合:
#   KEY_PATH=~/.ssh/my-key.pem bash infra/scripts/deploy-backend.sh

set -euo pipefail

# ── 必須環境変数チェック ────────────────────────────────────────────────
if [[ -z "${DB_PASSWORD:-}" ]]; then
  echo "エラー: DB_PASSWORD が未設定です。"
  echo "  export DB_PASSWORD='<パスワード>' を実行してから再試行してください。"
  exit 1
fi

if [[ -z "${JWT_SECRET:-}" ]]; then
  echo "エラー: JWT_SECRET が未設定です（32文字以上のランダム文字列が必要）。"
  echo "  export JWT_SECRET='\$(openssl rand -hex 32)' を実行してから再試行してください。"
  exit 1
fi

if [[ "${#JWT_SECRET}" -lt 32 ]]; then
  echo "エラー: JWT_SECRET は32文字以上にしてください（現在: ${#JWT_SECRET}文字）。"
  exit 1
fi

# ── 設定 ────────────────────────────────────────────────────────────────
REPO_ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
KEY_PATH="${KEY_PATH:-$HOME/.ssh/taskmanagement.pem}"
TERRAFORM_DIR="$REPO_ROOT/infra/terraform"
BACKEND_DIR="$REPO_ROOT/backend"
SERVICE_NAME="taskmanagement-backend"
REMOTE_JAR_PATH="/home/ec2-user/app.jar"

# ── Step 1: Terraform output から接続先を取得 ────────────────────────────
echo "=== EC2 IP・RDSエンドポイントを取得 ==="
EC2_IP=$(terraform -chdir="$TERRAFORM_DIR" output -raw ec2_public_ip)
# RDS endpoint は "hostname:port" 形式で返るためホスト名のみ抽出
RDS_ENDPOINT_FULL=$(terraform -chdir="$TERRAFORM_DIR" output -raw rds_endpoint)
RDS_HOST="${RDS_ENDPOINT_FULL%%:*}"
echo "EC2 IP    : $EC2_IP"
echo "RDS Host  : $RDS_HOST"

# ── Step 2: JAR をローカルでビルド ───────────────────────────────────────
echo ""
echo "=== バックエンドをビルド ==="
cd "$BACKEND_DIR"
./gradlew bootJar --no-daemon -q
JAR_FILE=$(ls build/libs/*.jar | grep -v plain | head -1)
echo "JAR: $JAR_FILE"

# ── Step 3: JAR を EC2 に転送 ────────────────────────────────────────────
echo ""
echo "=== JARをEC2に転送 ==="
scp -i "$KEY_PATH" \
    -o StrictHostKeyChecking=no \
    "$JAR_FILE" \
    "ec2-user@$EC2_IP:$REMOTE_JAR_PATH"

# ── Step 4: systemd サービスを設定・起動 ─────────────────────────────────
echo ""
echo "=== systemd サービスを設定 ==="
ssh -i "$KEY_PATH" -o StrictHostKeyChecking=no "ec2-user@$EC2_IP" \
    bash -s -- "$RDS_HOST" "$DB_PASSWORD" "$JWT_SECRET" "$SERVICE_NAME" "$REMOTE_JAR_PATH" << 'SSHEOF'
RDS_HOST="$1"
DB_PASSWORD="$2"
JWT_SECRET="$3"
SERVICE_NAME="$4"
REMOTE_JAR_PATH="$5"

# systemd ユニットファイルを生成
sudo tee "/etc/systemd/system/${SERVICE_NAME}.service" > /dev/null << UNITEOF
[Unit]
Description=TaskManagement Backend (Spring Boot)
After=network.target

[Service]
Type=simple
User=ec2-user
Environment="DB_HOST=${RDS_HOST}"
Environment="DB_PORT=5432"
Environment="DB_NAME=taskmanagement"
Environment="DB_USER=postgres"
Environment="DB_PASSWORD=${DB_PASSWORD}"
Environment="JWT_SECRET=${JWT_SECRET}"
ExecStart=/usr/bin/java -jar ${REMOTE_JAR_PATH}
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
UNITEOF

sudo systemctl daemon-reload
sudo systemctl enable "$SERVICE_NAME"
sudo systemctl restart "$SERVICE_NAME"

echo "サービス起動中... (5秒待機)"
sleep 5
sudo systemctl status "$SERVICE_NAME" --no-pager -l
SSHEOF

# ── Step 5: 疎通確認 ─────────────────────────────────────────────────────
echo ""
echo "=== API疎通確認 ==="
# nginx 経由（ポート80）でヘルスチェック
HTTP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" \
    --max-time 15 \
    "http://$EC2_IP/api/boards" || true)
echo "GET /api/boards → HTTP $HTTP_STATUS"

echo ""
echo "デプロイ完了"
echo "  フロントエンド : http://$EC2_IP"
echo "  バックエンドAPI: http://$EC2_IP/api/"
echo ""
echo "ログ確認（EC2にSSH後）:"
echo "  sudo journalctl -u $SERVICE_NAME -f"
