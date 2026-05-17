# TaskManagement — セットアップからAWSデプロイまで完全ガイド

このドキュメントは、プロジェクトのローカル開発環境の構築から AWS 本番環境へのデプロイまでの全手順をまとめたものです。

---

## 目次

1. [プロジェクト概要](#1-プロジェクト概要)
2. [前提条件](#2-前提条件)
3. [ローカル開発環境のセットアップ](#3-ローカル開発環境のセットアップ)
4. [開発フロー（コードを書くとき）](#4-開発フローコードを書くとき)
5. [AWS 事前準備](#5-aws-事前準備)
6. [Terraform でインフラを構築する](#6-terraform-でインフラを構築する)
7. [フロントエンドをデプロイする](#7-フロントエンドをデプロイする)
8. [バックエンドをデプロイする](#8-バックエンドをデプロイする)
9. [動作確認](#9-動作確認)
10. [運用：停止・再開・削除](#10-運用停止再開削除)
11. [再デプロイの手順](#11-再デプロイの手順)
12. [トラブルシューティング](#12-トラブルシューティング)

---

## 1. プロジェクト概要

### 技術スタック

```
┌─────────────────────────────────────────────────┐
│                   ユーザー (ブラウザ)              │
└─────────────────────┬───────────────────────────┘
                      │ HTTP :80
┌─────────────────────▼───────────────────────────┐
│  EC2 (Amazon Linux 2023)                        │
│  ├── nginx :80                                  │
│  │    ├── /       → React 静的ファイル配信        │
│  │    └── /api/   → Spring Boot へ転送           │
│  └── Spring Boot :8080                          │
└─────────────────────┬───────────────────────────┘
                      │ PostgreSQL :5432
┌─────────────────────▼───────────────────────────┐
│  RDS (PostgreSQL 16) ※プライベートサブネット      │
└─────────────────────────────────────────────────┘
```

| 役割 | 技術 | ポート |
|---|---|---|
| フロントエンド | React 19 + Vite | 5173（開発）/ 80（本番） |
| バックエンド | Spring Boot 3.4 + Java 21 | 8080 |
| データベース | PostgreSQL 16 | 5432 |
| インフラ | AWS EC2 + RDS + Terraform | — |

### ディレクトリ構成

```
TaskManagement/
├── frontend/                  # React アプリ
├── backend/                   # Spring Boot アプリ
├── infra/
│   ├── terraform/             # AWS インフラ定義
│   └── scripts/               # デプロイスクリプト
│       ├── deploy-frontend.sh
│       └── deploy-backend.sh
├── docs/                      # ドキュメント
└── docker-compose.yml         # ローカル DB 用
```

---

## 2. 前提条件

以下のツールをインストールしておく。

| ツール | 確認コマンド | 推奨バージョン |
|---|---|---|
| Node.js | `node -v` | 18 以上 |
| Java | `java -version` | 21 以上 |
| Docker | `docker -v` | 最新 |
| Git | `git -v` | 最新 |
| Terraform | `terraform -v` | 1.5.0 以上 |
| AWS CLI | `aws --version` | v2 |
| GitHub CLI | `gh --version` | 最新 |

---

## 3. ローカル開発環境のセットアップ

### 3-1. リポジトリをクローン

```bash
git clone https://github.com/akki-secure/TaskManagement.git
cd TaskManagement
```

### 3-2. フロントエンドの依存パッケージをインストール

```bash
cd frontend
npm install
cd ..
```

### 3-3. ローカル環境を起動する

#### 方法 A: Claude Code スキルで一括起動（推奨）

Claude Code を使っている場合は 1 コマンドで起動できる。

```
/start
```

DB・バックエンド・フロントエンドの起動確認まで自動で行う。

#### 方法 B: 手動で起動

**ターミナル 1 — データベース（Docker）**

```bash
docker compose up -d
docker compose ps   # State: running を確認
```

**ターミナル 2 — バックエンド**

```bash
cd backend
./gradlew bootRun
# 起動確認 → http://localhost:8080/api/boards
```

**ターミナル 3 — フロントエンド**

```bash
cd frontend
npm run dev
# ブラウザで → http://localhost:5173
```

### 3-4. 停止する

各ターミナルで `Ctrl + C` を押す。DB コンテナを止める場合:

```bash
docker compose down
```

---

## 4. 開発フロー（コードを書くとき）

このプロジェクトでは **イシューファースト** のルールを徹底している。コードを書く前に必ずイシューを作ること。

```
① GitHub イシュー作成
    ↓
② ブランチを作る（main から）
    ↓
③ 実装
    ↓
④ コミット（Refs #番号 を含める）
    ↓
⑤ プッシュ → PR 作成（Closes #番号 を含める）
    ↓
⑥ PR をマージ
```

### コマンド例

```bash
# 既存イシューを確認
gh issue list --state open

# イシューを作成
gh issue create --title "feat: カードに期限日を追加" --label "feature"
# → イシュー番号が発行される（例: #12）

# main から作業ブランチを作る
git checkout main && git pull origin main
git checkout -b feature/issue-12-add-card-due-date

# ... 実装 ...

# コミット
git add <ファイル>
git commit -m "feat: カードに期限日設定機能を追加

Refs #12"

# プッシュ → PR 作成
git push origin feature/issue-12-add-card-due-date
gh pr create --title "feat: カードに期限日設定機能を追加" --base main
# PR 本文に「Closes #12」を含める
```

### ブランチ命名規則

| prefix | 用途 |
|---|---|
| `feature/issue-<番号>-<説明>` | 新機能 |
| `fix/issue-<番号>-<説明>` | バグ修正 |
| `chore/issue-<番号>-<説明>` | 設定・依存関係 |
| `docs/issue-<番号>-<説明>` | ドキュメント |

---

## 5. AWS 事前準備

インフラを構築する前に以下を用意する。

### 5-1. AWS CLI の認証設定

```bash
aws configure
# AWS Access Key ID     : <IAMユーザーのアクセスキー>
# AWS Secret Access Key : <シークレットキー>
# Default region name   : ap-northeast-1
# Default output format : json

# 確認
aws sts get-caller-identity
```

### 5-2. EC2 キーペアを作成

AWS コンソール → EC2 → キーペア → 「キーペアを作成」

- 名前: `taskmanagement`（任意）
- 形式: `.pem`
- ダウンロードして `~/.ssh/taskmanagement.pem` に保存

```bash
chmod 400 ~/.ssh/taskmanagement.pem
```

### 5-3. 自分の IP アドレスを確認

```bash
curl https://checkip.amazonaws.com
# → 例: 203.0.113.45
```

この IP を `terraform.tfvars` の `allowed_ip` に設定する（次のステップで使う）。

---

## 6. Terraform でインフラを構築する

### 6-1. tfvars ファイルを作成

```bash
cd infra/terraform
cp terraform.tfvars.example terraform.tfvars
```

`terraform.tfvars` を編集する（git 管理外のため安全）:

```hcl
aws_region    = "ap-northeast-1"
project_name  = "taskmanagement"
key_pair_name = "taskmanagement"          # 5-2 で作ったキーペア名
allowed_ip    = "203.0.113.45/32"         # 5-3 で確認した自分のIP

db_name       = "taskmanagement"
db_username   = "postgres"
db_password   = "任意の強力なパスワード"   # 必ず変更する

tfstate_bucket = "taskmanagement-tfstate-<AWSアカウントID>"
```

> AWSアカウントIDは `aws sts get-caller-identity --query Account --output text` で確認できる。

### 6-2. S3 バケットを作成（tfstate 保存用）

> **初回のみ実施。** tfstate を安全に S3 で管理するためのバケットを作成する。

```bash
ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
BUCKET_NAME="taskmanagement-tfstate-${ACCOUNT_ID}"

# バケット作成
aws s3api create-bucket \
  --bucket "$BUCKET_NAME" \
  --region ap-northeast-1 \
  --create-bucket-configuration LocationConstraint=ap-northeast-1

# バージョニング・暗号化・パブリックアクセスブロックを有効化
aws s3api put-bucket-versioning \
  --bucket "$BUCKET_NAME" \
  --versioning-configuration Status=Enabled

aws s3api put-bucket-encryption \
  --bucket "$BUCKET_NAME" \
  --server-side-encryption-configuration \
  '{"Rules":[{"ApplyServerSideEncryptionByDefault":{"SSEAlgorithm":"AES256"}}]}'

aws s3api put-public-access-block \
  --bucket "$BUCKET_NAME" \
  --public-access-block-configuration \
  "BlockPublicAcls=true,IgnorePublicAcls=true,BlockPublicPolicy=true,RestrictPublicBuckets=true"
```

### 6-3. Terraform を初期化

```bash
# infra/terraform ディレクトリで実行
terraform init -backend-config="bucket=taskmanagement-tfstate-<AWSアカウントID>"
```

### 6-4. インフラを構築

```bash
# 差分確認（何が作られるかプレビュー）
terraform plan

# 実際に構築（確認プロンプトに yes と入力）
terraform apply
```

完了すると以下が出力される:

```
ec2_public_ip = "xx.xx.xx.xx"
frontend_url  = "http://xx.xx.xx.xx"
backend_url   = "http://xx.xx.xx.xx:8080"
rds_endpoint  = "taskmanagement-db.xxxxxxxx.ap-northeast-1.rds.amazonaws.com:5432"
ssh_command   = "ssh -i ~/.ssh/<キーペア名>.pem ec2-user@xx.xx.xx.xx"
```

EC2 には `user_data` により **Java 21・nginx が自動インストール**される。

---

## 7. フロントエンドをデプロイする

既存スクリプト `infra/scripts/deploy-frontend.sh` を使う。

```bash
# プロジェクトルートから実行
KEY_PATH=~/.ssh/taskmanagement.pem bash infra/scripts/deploy-frontend.sh
```

**スクリプトが自動でやること:**

1. `terraform output` で EC2 の IP を取得
2. `npm run build` で React をビルド（`frontend/dist/` に出力）
3. SCP で EC2 の `/tmp/frontend-dist/` に転送
4. SSH で `/usr/share/nginx/html/` にコピー
5. `nginx reload` で反映

完了後に `http://<EC2_IP>` でフロントエンドが表示される。

> **注意:** 本番ビルドでは Vite の dev proxy（`/api → localhost:8080`）は無効になる。代わりに nginx が `/api/` を Spring Boot へ転送するため、フロントエンドのコード変更は不要。

---

## 8. バックエンドをデプロイする

### 8-1. シークレットを環境変数にセット

```bash
# DB パスワード（terraform.tfvars に設定した db_password と同じ値）
export DB_PASSWORD='terraform.tfvarsに設定したパスワード'

# JWT シークレット（本番用・32文字以上。生成コマンドをそのまま使うと楽）
export JWT_SECRET="$(openssl rand -hex 32)"

# 確認
echo "DB_PASSWORD: ${DB_PASSWORD:0:3}***"
echo "JWT_SECRET length: ${#JWT_SECRET}"
```

> **JWT_SECRET は必ず保存しておくこと。**  
> 再デプロイ時に異なる値を使うと、既存のログインユーザーが全員ログアウトされる。  
> パスワードマネージャーや `.env.production`（gitignore 対象）などに記録しておく。

### 8-2. デプロイスクリプトを実行

```bash
# プロジェクトルートから実行
KEY_PATH=~/.ssh/taskmanagement.pem bash infra/scripts/deploy-backend.sh
```

**スクリプトが自動でやること:**

1. `terraform output` で EC2 IP・RDS エンドポイントを取得
2. `./gradlew bootJar` で JAR をビルド
3. SCP で EC2 の `~/app.jar` に転送
4. SSH で systemd ユニットファイルを生成（DB 接続情報を環境変数で注入）
5. `systemctl enable & restart` でサービス起動
6. `/api/boards` の HTTP ステータスで疎通確認

### 8-3. EC2 上のサービス管理

```bash
# SSH で EC2 に入る
ssh -i ~/.ssh/taskmanagement.pem ec2-user@<EC2_IP>

# サービスの状態確認
sudo systemctl status taskmanagement-backend

# リアルタイムログ
sudo journalctl -u taskmanagement-backend -f

# 再起動
sudo systemctl restart taskmanagement-backend
```

---

## 9. 動作確認

```bash
EC2_IP=$(terraform -chdir=infra/terraform output -raw ec2_public_ip)

# フロントエンド（ブラウザで確認）
open "http://$EC2_IP"

# バックエンド API の疎通確認
curl -s -o /dev/null -w "%{http_code}" "http://$EC2_IP/api/boards"
# → 401（認証が必要）または 200 が返れば正常
```

| HTTP レスポンス | 状態 |
|---|---|
| `401` | バックエンド正常動作（認証が必要なエンドポイント） |
| `200` | バックエンド正常動作 |
| `502` | バックエンドが起動していない（ログを確認） |
| `404` | nginx は動いているが設定ミス |

---

## 10. 運用：停止・再開・削除

### 使わないときの費用節約

```
しばらく使わない（数日〜1週間）
  → EC2・RDS を「停止」する（データは残る、ストレージ料金のみ）

長期間使わない / 環境をリセットしたい
  → terraform destroy で全削除（費用ゼロ、再構築は apply 1コマンド）
```

### EC2 の停止・再開

```bash
EC2_ID=$(aws ec2 describe-instances \
  --filters "Name=tag:Name,Values=taskmanagement-server" \
  --query "Reservations[0].Instances[0].InstanceId" \
  --output text --region ap-northeast-1)

# 停止
aws ec2 stop-instances --instance-ids $EC2_ID --region ap-northeast-1

# 再開（※ パブリック IP が変わることに注意）
aws ec2 start-instances --instance-ids $EC2_ID --region ap-northeast-1
```

> EC2 再開後はパブリック IP が変わる。新しい IP は AWS コンソールで確認する。

### RDS の停止・再開

```bash
# 停止（最大 7 日間。7 日経過すると AWS が自動的に再起動する）
aws rds stop-db-instance \
  --db-instance-identifier taskmanagement-db --region ap-northeast-1

# 再開
aws rds start-db-instance \
  --db-instance-identifier taskmanagement-db --region ap-northeast-1

# 状態確認（stopped / starting / available）
aws rds describe-db-instances \
  --db-instance-identifier taskmanagement-db \
  --query "DBInstances[0].DBInstanceStatus" \
  --output text --region ap-northeast-1
```

### 完全削除（terraform destroy）

```bash
cd infra/terraform

# 削除前に手動スナップショットを取る（任意だが推奨）
aws rds create-db-snapshot \
  --db-instance-identifier taskmanagement-db \
  --db-snapshot-identifier taskmanagement-db-backup-$(date +%Y%m%d) \
  --region ap-northeast-1

# 全リソースを削除
terraform destroy
```

> `rds.tf` の `skip_final_snapshot = false` により、destroy 時に自動スナップショット（`taskmanagement-db-final-snapshot`）も作成される。

---

## 11. 再デプロイの手順

コードを変更して再デプロイする場合の手順。

### フロントエンドのみ更新

```bash
KEY_PATH=~/.ssh/taskmanagement.pem bash infra/scripts/deploy-frontend.sh
```

### バックエンドのみ更新

```bash
# DB_PASSWORD・JWT_SECRET を同じ値でセット（初回デプロイ時と同じ値を使う）
export DB_PASSWORD='...'
export JWT_SECRET='...'   # 保存しておいた値を使う

KEY_PATH=~/.ssh/taskmanagement.pem bash infra/scripts/deploy-backend.sh
```

### terraform destroy 後に再構築する場合

```bash
# インフラを再構築
cd infra/terraform
terraform apply

# フロントエンドをデプロイ
KEY_PATH=~/.ssh/taskmanagement.pem bash infra/scripts/deploy-frontend.sh

# バックエンドをデプロイ
export DB_PASSWORD='...'
export JWT_SECRET='...'
KEY_PATH=~/.ssh/taskmanagement.pem bash infra/scripts/deploy-backend.sh
```

> RDS のデータはスナップショットから復元できる（AWS コンソール → RDS → スナップショット → 復元）。

---

## 12. トラブルシューティング

### サイトが表示されない（502 Bad Gateway）

バックエンドが起動していない。

```bash
# EC2 に SSH してログを確認
ssh -i ~/.ssh/taskmanagement.pem ec2-user@<EC2_IP>
sudo journalctl -u taskmanagement-backend -n 50 --no-pager
```

よくある原因:

| エラーメッセージ | 原因 | 対処 |
|---|---|---|
| `PSQLException: Connection timed out` | RDS が停止している | RDS を起動してからバックエンドを再起動 |
| `PSQLException: password authentication failed` | DB_PASSWORD が間違っている | 正しいパスワードで再デプロイ |
| `IllegalArgumentException: JWT secret too short` | JWT_SECRET が 32 文字未満 | `openssl rand -hex 32` で生成し直す |

### RDS に繋がらない

RDS が停止していないか確認する。

```bash
aws rds describe-db-instances \
  --db-instance-identifier taskmanagement-db \
  --query "DBInstances[0].DBInstanceStatus" \
  --output text --region ap-northeast-1
# → stopped の場合は起動する

aws rds start-db-instance \
  --db-instance-identifier taskmanagement-db --region ap-northeast-1

# 起動完了（available）を待ってからバックエンドを再起動
ssh -i ~/.ssh/taskmanagement.pem ec2-user@<EC2_IP> \
  "sudo systemctl restart taskmanagement-backend"
```

### EC2 に SSH できない

IP アドレスが変わっている可能性がある。

```bash
# 現在の EC2 パブリック IP を確認
aws ec2 describe-instances \
  --filters "Name=tag:Name,Values=taskmanagement-server" \
  --query "Reservations[0].Instances[0].PublicIpAddress" \
  --output text --region ap-northeast-1
```

自分の IP が変わっている場合は `terraform.tfvars` の `allowed_ip` を更新して `terraform apply` を再実行する。

### export コマンドで `dquote>` と表示される

パスワードに `!` などの特殊文字が含まれている。**シングルクォート** `'` を使う。

```bash
# NG（ダブルクォートは ! を特殊文字として解釈する）
export DB_PASSWORD="password123!"

# OK（シングルクォートは全文字をそのまま扱う）
export DB_PASSWORD='password123!'
```
