CREATE TABLE IF NOT EXISTS boards (
    id    BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS lists (
    id       BIGSERIAL PRIMARY KEY,
    board_id BIGINT      NOT NULL REFERENCES boards(id) ON DELETE CASCADE,
    title    VARCHAR(255) NOT NULL,
    position INTEGER      NOT NULL
);

CREATE TABLE IF NOT EXISTS cards (
    id          BIGSERIAL PRIMARY KEY,
    list_id     BIGINT       NOT NULL REFERENCES lists(id) ON DELETE CASCADE,
    title       VARCHAR(255) NOT NULL DEFAULT '無題のカード',
    description TEXT,
    due_date    DATE,
    priority    VARCHAR(10),
    position    INTEGER      NOT NULL
);
