---
--- Adapted from https://github.com/excellaco/tcp-java/blob/master/src/main/resources/db/migration/V2__CreateSkillTables.sql
---

CREATE TABLE skill_category (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

CREATE TABLE skill (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    category_id INTEGER
);

ALTER TABLE skill
ADD CONSTRAINT fk_skill_skcat
FOREIGN KEY (category_id) REFERENCES skill_category(id)
ON DELETE CASCADE;

CREATE INDEX idx_skill_skcat
ON skill(category_id);
