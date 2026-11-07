-- アプリ専用ユーザーを作成（最小権限: 対象DBのみ）。初回のデータ初期化時のみ実行される
\getenv app_user APP_DB_USER
\getenv app_pw APP_DB_PASSWORD
\getenv db POSTGRES_DB
CREATE ROLE :"app_user" LOGIN PASSWORD :'app_pw';
GRANT ALL PRIVILEGES ON DATABASE :"db" TO :"app_user";
GRANT ALL ON SCHEMA public TO :"app_user";
