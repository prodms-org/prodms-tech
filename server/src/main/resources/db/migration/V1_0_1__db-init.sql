CREATE TABLE IF NOT EXISTS equipments (
    id                  SERIAL                              PRIMARY KEY,
    number              VARCHAR(100)                        NOT NULL        UNIQUE,
    name                VARCHAR(100)                        NOT NULL,
    type                SMALLINT                            NOT NULL        CHECK (type >= 0 AND type <= 5),
    created_at          TIMESTAMP WITH TIME ZONE            NOT NULL,
    updated_at          TIMESTAMP WITH TIME ZONE            NOT NULL
);

CREATE TABLE IF NOT EXISTS equipments_sets (
    id                  SERIAL                              PRIMARY KEY,
    number              VARCHAR(100)                        NOT NULL        UNIQUE,
    name                VARCHAR(100)                        NOT NULL,
    description         TEXT                                NOT NULL,
    created_at          TIMESTAMP WITH TIME ZONE            NOT NULL,
    updated_at          TIMESTAMP WITH TIME ZONE            NOT NULL
);

CREATE TABLE IF NOT EXISTS equipments_sets_composition (
    equipment_id        INT                             REFERENCES equipments(id)        ON DELETE CASCADE,
    set_id              INT                             REFERENCES equipments_sets(id)   ON DELETE CASCADE,
    created_at          TIMESTAMP WITH TIME ZONE        NOT NULL,
    updated_at          TIMESTAMP WITH TIME ZONE        NOT NULL,
    PRIMARY KEY (equipment_id, set_id)
);

CREATE TABLE IF NOT EXISTS blanks (
    id                  SERIAL                                  PRIMARY KEY,
    number              VARCHAR(100)                            NOT NULL    UNIQUE,
    material            VARCHAR(100)                            NOT NULL,
    params              JSONB,
    created_at          TIMESTAMP WITH TIME ZONE                NOT NULL,
    updated_at          TIMESTAMP WITH TIME ZONE                NOT NULL
);

CREATE TABLE IF NOT EXISTS processes (
    id                  SERIAL                                  PRIMARY KEY,
    number              VARCHAR(100)                            NOT NULL    UNIQUE,
    unit                VARCHAR(100)                            NOT NULL,
    created_at          TIMESTAMP WITH TIME ZONE                NOT NULL,
    updated_at          TIMESTAMP WITH TIME ZONE                NOT NULL,
    priority            SMALLINT                                NOT NULL,
    blank_id            INT                                     REFERENCES blanks(id),
    UNIQUE (number, priority)
);

CREATE TABLE IF NOT EXISTS processes_steps (
    id                  SERIAL                                  PRIMARY KEY,
    number              VARCHAR(100)                            NOT NULL,
    equipment_set_id    INT                                     REFERENCES equipments_sets(id),
    process_id          INT                                     REFERENCES processes(id),
    order_num           SMALLINT                                NOT NULL,
    times               JSONB,
    created_at          TIMESTAMP WITH TIME ZONE                NOT NULL,
    updated_at          TIMESTAMP WITH TIME ZONE                NOT NULL
);




















