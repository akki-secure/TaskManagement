---
description: TaskManagementプロジェクトのDB・バックエンド・フロントエンドをすべて起動する
---

以下の手順でTaskManagementプロジェクトのサーバーをすべて起動してください。各ステップを順番に実行し、起動を確認してから次へ進んでください。

## Step 1: データベース（PostgreSQL）を起動

プロジェクトルート（/Users/aki/Desktop/TaskManagement）で以下を実行してください:

```bash
docker compose up -d --wait
```

`--wait` によりヘルスチェックが通るまで待機するため、完了後は即座に次のステップへ進んでください。

## Step 2: バックエンド（Spring Boot）を起動

**注意:** システムデフォルトのJavaでビルドできない場合があるため、Java 21を明示指定する。パスが異なる場合は `$(/usr/libexec/java_home -v 21)` で確認すること。

以下のコマンドをバックグラウンドで実行してください:

```bash
cd backend && JAVA_HOME=$(/usr/libexec/java_home -v 21) ./gradlew bootRun > /tmp/backend.log 2>&1 &
```

起動完了まで待機してください:

```bash
until grep -q "Started TaskManagementApplication" /tmp/backend.log 2>/dev/null; do sleep 2; done && echo "Backend ready"
```

ポートは **8080** です。

## Step 3: フロントエンド（React + Vite）を起動

以下のコマンドをバックグラウンドで実行してください:

```bash
cd frontend && npm run dev > /tmp/frontend.log 2>&1 &
```

ログを確認し、`Local: http://localhost:5173` が出力されたら起動完了です:

```bash
until grep -q "Local:" /tmp/frontend.log 2>/dev/null; do sleep 1; done && echo "Frontend ready"
```

## 完了報告

全サービスが起動したら、以下の情報をユーザーに報告してください:

| サービス | URL | 状態 |
|---|---|---|
| フロントエンド（カンバンボード） | http://localhost:5173 | ✅ 起動済み |
| バックエンドAPI | http://localhost:8080/api/boards | ✅ 起動済み |
| DB（PostgreSQL） | localhost:5432 | ✅ 起動済み |

## サービス停止手順

各サービスを停止する場合:

```bash
lsof -ti :5173
lsof -ti :8080
docker compose down
```

**注意:** `kill` / `pkill` は `.claude/settings.json` でブロックされている。PIDを取得してユーザーに伝え、手動で `kill <PID>` を実行してもらうこと。

## トラブルシューティング

問題が発生した場合:
- DB接続エラー → `docker compose ps` で確認後 `docker compose up -d --wait` を再実行
- ポート競合 → 上記「サービス停止手順」を参照してPIDをユーザーに伝える
- Gradleビルド失敗 → `cd backend && JAVA_HOME=$(/usr/libexec/java_home -v 21) ./gradlew clean bootRun` でクリーンビルド
- Java バージョンエラー → `JAVA_HOME` の指定を確認（Java 21必須。`/usr/libexec/java_home -v 21` でパスを取得）
