-- =====================================================
-- 完全なテーブル定義（新規インストール用）
-- 既存DBには下部の ALTER TABLE でカラムが追加される
-- =====================================================

CREATE TABLE IF NOT EXISTS users (
    id            BIGSERIAL    PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255),
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS boards (
    id      BIGSERIAL    PRIMARY KEY,
    user_id BIGINT       REFERENCES users(id) ON DELETE CASCADE,
    title   VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS lists (
    id         BIGSERIAL    PRIMARY KEY,
    board_id   BIGINT       NOT NULL REFERENCES boards(id) ON DELETE CASCADE,
    title      VARCHAR(255) NOT NULL,
    position   INTEGER      NOT NULL
);

CREATE TABLE IF NOT EXISTS cards (
    id          BIGSERIAL    PRIMARY KEY,
    list_id     BIGINT       NOT NULL REFERENCES lists(id) ON DELETE CASCADE,
    title       VARCHAR(255) NOT NULL DEFAULT '無題のカード',
    description TEXT,
    due_date    DATE,
    priority    VARCHAR(10)  CHECK (priority IN ('high', 'medium', 'low')),
    status      VARCHAR(20)  NOT NULL DEFAULT 'todo'
                             CHECK (status IN ('todo', 'in_progress', 'done')),
    position    INTEGER      NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS passkey_credentials (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT    NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    credential_id   TEXT      NOT NULL UNIQUE,
    public_key_cose BYTEA     NOT NULL,
    sign_count      BIGINT    NOT NULL DEFAULT 0,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

-- インデックス（パフォーマンス向上）
CREATE INDEX IF NOT EXISTS idx_boards_user_id   ON boards(user_id);
CREATE INDEX IF NOT EXISTS idx_lists_board_id   ON lists(board_id);
CREATE INDEX IF NOT EXISTS idx_cards_list_id    ON cards(list_id);

-- =====================================================
-- 既存DBへのマイグレーション（冪等: 列が存在しなければ追加）
-- =====================================================

ALTER TABLE boards ADD COLUMN IF NOT EXISTS user_id BIGINT REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE cards ADD COLUMN IF NOT EXISTS status     VARCHAR(20)  NOT NULL DEFAULT 'todo';
ALTER TABLE cards ADD COLUMN IF NOT EXISTS created_at TIMESTAMP    NOT NULL DEFAULT NOW();
ALTER TABLE cards ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP    NOT NULL DEFAULT NOW();
