# TaskManagement

Trello風のタスク管理アプリです。

## 必要な環境

- Node.js（インストールされていない場合は https://nodejs.org からダウンロード）
- Java 17以上（バックエンドを起動する場合）
- PostgreSQL（バックエンドを起動する場合）

## 起動方法

### フロントエンド（localhost:3000）

1. このリポジトリをクローン
   ```
   git clone <リポジトリURL>
   ```

2. プロジェクトフォルダに移動
   ```
   cd TaskManagement
   ```

3. アプリを起動
   ```
   npm start
   ```

4. ブラウザで以下のURLを開く
   ```
   http://localhost:3000
   ```

### バックエンド（localhost:8080）

npm を使わずに Spring Boot サーバーを起動する場合は、`backend` フォルダで以下を実行します。

```
cd backend
./gradlew bootRun
```

Windows の場合:
```
cd backend
gradlew.bat bootRun
```

ブラウザまたは API クライアントで以下の URL にアクセスできます:
```
http://localhost:8080
```

> PostgreSQL が起動済みであることを確認してください。接続設定は `backend/src/main/resources/application.properties` で変更できます。

## 終了方法

ターミナルで以下のキーを押す

- Mac: `Command + C`
- Windows: `Ctrl + C`
