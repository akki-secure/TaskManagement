---
description: TaskManagementプロジェクトのDB・バックエンド・フロントエンドをすべて起動する
---

以下の手順でTaskManagementプロジェクトのサーバーをすべて起動してください。各ステップを順番に実行し、起動を確認してから次へ進んでください。

## Step 1: データベース（PostgreSQL）を起動

プロジェクトルート（/Users/aki/Desktop/TaskManagement）で以下を実行してください:

```bash
docker compose up -d
```

その後、コンテナの状態を確認してください:

```bash
docker compose ps
```

`taskmanagement-db` の State が `running` であることを確認してください。

## Step 2: バックエンド（Spring Boot）を起動

以下のコマンドをバックグラウンドで実行してください:

```bash
cd backend && ./gradlew bootRun > /tmp/backend.log 2>&1 &
```

ログを監視し、`Started TaskManagementApplication` が出力されるまで待ってください:

```bash
tail -f /tmp/backend.log
```

起動確認できたらStep 3へ進んでください。ポートは **8080** です。

## Step 3: フロントエンド（React + Vite）を起動

以下のコマンドをバックグラウンドで実行してください:

```bash
cd frontend && npm run dev > /tmp/frontend.log 2>&1 &
```

ログを確認し、`Local: http://localhost:5173` が出力されたら起動完了です:

```bash
tail /tmp/frontend.log
```

## 完了報告

全サービスが起動したら、以下の情報をユーザーに報告してください:

| サービス | URL | 状態 |
|---|---|---|
| フロントエンド（カンバンボード） | http://localhost:5173 | ✅ 起動済み |
| バックエンドAPI | http://localhost:8080/api/boards | ✅ 起動済み |
| DB（PostgreSQL） | localhost:5432 | ✅ 起動済み |

## トラブルシューティング

問題が発生した場合:
- DB接続エラー → `docker compose ps` で確認後 `docker compose up -d` を再実行
- ポート競合 → `lsof -i :8080` または `lsof -i :5173` でプロセスを確認しkill
- Gradleビルド失敗 → `cd backend && ./gradlew clean bootRun` でクリーンビルド
