CREATE TABLE timelines (
    id          UUID         NOT NULL PRIMARY KEY,
    name        VARCHAR(200) NOT NULL,
    description TEXT,
    visibility  VARCHAR(20)  NOT NULL DEFAULT 'PRIVATE',
    created_at  TIMESTAMPTZ  NOT NULL,
    updated_at  TIMESTAMPTZ  NOT NULL
);
