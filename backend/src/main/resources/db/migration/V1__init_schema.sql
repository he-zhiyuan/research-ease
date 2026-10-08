CREATE TABLE sys_user (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    username        VARCHAR(50)  NOT NULL UNIQUE,
    password_hash   VARCHAR(100) NOT NULL,
    real_name       VARCHAR(50)  NOT NULL,
    role            VARCHAR(20)  NOT NULL,
    enabled         TINYINT(1)   NOT NULL DEFAULT 1,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE project (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_name        VARCHAR(200) NOT NULL,
    research_content    TEXT,
    leader_id           BIGINT NOT NULL,
    team_members        VARCHAR(500),
    funding_source      VARCHAR(200),
    start_date          DATE,
    end_date            DATE,
    status              VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    submitted_at        DATETIME NULL,
    created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_project_leader FOREIGN KEY (leader_id) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE budget_item (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id          BIGINT NOT NULL,
    category            VARCHAR(30) NOT NULL,
    requested_amount     DECIMAL(14,2) NOT NULL,
    approved_amount      DECIMAL(14,2) NULL,
    created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_budget_item_project FOREIGN KEY (project_id) REFERENCES project (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE expenditure (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id      BIGINT NOT NULL,
    budget_item_id  BIGINT NOT NULL,
    amount          DECIMAL(14,2) NOT NULL,
    description     VARCHAR(500),
    status          VARCHAR(10) NOT NULL DEFAULT 'PENDING',
    created_by      BIGINT NOT NULL,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_expenditure_project FOREIGN KEY (project_id) REFERENCES project (id),
    CONSTRAINT fk_expenditure_budget_item FOREIGN KEY (budget_item_id) REFERENCES budget_item (id),
    CONSTRAINT fk_expenditure_creator FOREIGN KEY (created_by) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE progress_report (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id  BIGINT NOT NULL,
    content     TEXT NOT NULL,
    report_date DATE NOT NULL,
    created_by  BIGINT NOT NULL,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_progress_report_project FOREIGN KEY (project_id) REFERENCES project (id),
    CONSTRAINT fk_progress_report_creator FOREIGN KEY (created_by) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE achievement (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id      BIGINT NOT NULL,
    type            VARCHAR(20) NOT NULL,
    title           VARCHAR(300) NOT NULL,
    detail          VARCHAR(300),
    achieved_date   DATE,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_achievement_project FOREIGN KEY (project_id) REFERENCES project (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE approval_record (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id      BIGINT NOT NULL,
    type            VARCHAR(20) NOT NULL,
    target_id       BIGINT NULL,
    result          VARCHAR(10) NOT NULL,
    comment         VARCHAR(500),
    operator_id     BIGINT NOT NULL,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_approval_record_project FOREIGN KEY (project_id) REFERENCES project (id),
    CONSTRAINT fk_approval_record_operator FOREIGN KEY (operator_id) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_project_leader ON project (leader_id);
CREATE INDEX idx_project_status ON project (status);
CREATE INDEX idx_budget_item_project ON budget_item (project_id);
CREATE INDEX idx_expenditure_project ON expenditure (project_id);
CREATE INDEX idx_expenditure_budget_item ON expenditure (budget_item_id);
CREATE INDEX idx_expenditure_status ON expenditure (status);
CREATE INDEX idx_progress_report_project ON progress_report (project_id);
CREATE INDEX idx_achievement_project ON achievement (project_id);
CREATE INDEX idx_approval_record_project ON approval_record (project_id, type);
