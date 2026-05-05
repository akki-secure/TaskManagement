# TaskManagement

Trello風のカンバンボード型タスク管理アプリです。

チームやプロジェクトのタスクを「リスト（列）」と「カード」で視覚的に整理し、ドラッグ&ドロップで進捗を管理できます。カードには期限日・優先度（高/中/低）を設定でき、期限切れのカードは自動でハイライト表示されます。

## 機能

- ボードの作成・削除・名前変更
- リスト（カラム）の追加・削除・名前変更
- カードの作成・編集・削除
- カードへの期限日・優先度設定
- ドラッグ&ドロップによるカード移動

## 技術スタック

| 役割 | 技術 |
|---|---|
| フロントエンド | React 19 + Vite |
| バックエンド | Spring Boot 3.4 + Java 21 |
| データベース | PostgreSQL 16 |
| インフラ | Docker / docker-compose |

## 必要な環境

- Node.js 18以上
- Java 21以上
- Docker（PostgreSQL コンテナ起動用）

## 起動方法

### 一括起動（Claude Code スキル）

Claude Code を使っている場合は、以下のスキルコマンド一つで DB・バックエンド・フロントエンドをすべて起動できます。

```
/start
```

各サービスの起動確認まで自動で行います。詳細は [.claude/skills/start/SKILL.md](.claude/skills/start/SKILL.md) を参照してください。

### 手動起動

#### 1. データベース（PostgreSQL）

```bash
docker compose up -d
docker compose ps   # State: running を確認
```

#### 2. バックエンド（Spring Boot） — ポート 8080

```bash
cd backend
./gradlew bootRun
```

Windows の場合:
```bash
cd backend
gradlew.bat bootRun
```

起動確認:
```
http://localhost:8080/api/boards
```

#### 3. フロントエンド（React + Vite） — ポート 5173

```bash
cd frontend
npm install   # 初回のみ
npm run dev
```

ブラウザで開く:
```
http://localhost:5173
```

> バックエンドの接続設定は `backend/src/main/resources/application.properties` で変更できます。

## データベースの中身を直接確認する

コンテナが起動中であれば、以下の方法で PostgreSQL に接続してデータを確認できます。

### psql でコンテナに入る

```bash
docker exec -it taskmanagement-db psql -U postgres -d taskmanagement
```

接続後は通常の SQL が使えます。

```sql
-- テーブル一覧
\dt

-- ユーザー一覧
SELECT id, username, email FROM users;

-- ボード一覧
SELECT id, name, owner_id FROM boards;

-- リスト一覧
SELECT id, name, board_id, position FROM lists ORDER BY board_id, position;

-- カード一覧
SELECT id, title, list_id, due_date, priority FROM cards ORDER BY list_id;

-- psql を終了
\q
```

### 接続情報

| 項目 | 値 |
|---|---|
| ホスト | localhost |
| ポート | 5432 |
| データベース名 | taskmanagement |
| ユーザー | postgres |
| パスワード | postgres |

TablePlus・DBeaver などの GUI ツールでも上記の情報で接続できます。

---

## 終了方法

各ターミナルで `Ctrl + C`（Mac は `Command + C`）を押す。  
DB コンテナを停止する場合:

```bash
docker compose down
```

## Claude Code スキル

このプロジェクトには Claude Code 用のカスタムスキルが含まれています。

| スキル | コマンド | 説明 |
|---|---|---|
| 一括起動 | `/start` | DB・バックエンド・フロントエンドをすべて起動し、起動確認まで行う |

スキルのソースは [.claude/skills/](.claude/skills/) に格納されています。
