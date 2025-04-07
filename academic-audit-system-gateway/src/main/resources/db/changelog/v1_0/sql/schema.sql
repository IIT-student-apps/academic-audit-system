CREATE TABLE user_entity
(
    id       BIGSERIAL PRIMARY KEY,
    login    VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255),
    role     VARCHAR(50)
);

CREATE TABLE document_analyze_request
(
    id             UUID PRIMARY KEY,
    request_status VARCHAR(50),
    user_id        BIGINT NOT NULL,
    document_id    VARCHAR(255),
    report_data    TEXT,
    CONSTRAINT fk_document_request_user FOREIGN KEY (user_id)
        REFERENCES user_entity (id)
);

CREATE TABLE document_analyze_request_outbox_event
(
    id           BIGSERIAL PRIMARY KEY,
    is_published BOOLEAN NOT NULL,
    request_id   UUID    NOT NULL,
    CONSTRAINT fk_outbox_request FOREIGN KEY (request_id)
        REFERENCES document_analyze_request (id)
);
