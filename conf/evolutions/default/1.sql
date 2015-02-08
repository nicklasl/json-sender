# --- !Ups
CREATE TABLE document(
    name    CHAR(50)    NOT NULL    PRIMARY KEY,
    doc     JSON        NOT NULL
);

# --- !Downs

DROP TABLE document;