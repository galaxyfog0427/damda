-- V1: 표 만들기 (docs/erd.md 2~4번, 2026-11-14 승인)
-- 패스키 표 둘은 5주 차에 따로 만든다(ERD 2-5).
-- 규칙(ERD 0번)
--   - 표마다 문자 집합과 정렬 규칙을 직접 적는다(서버 기본값에 기대지 않음).
--   - 시각은 DATETIME(6), UTC. 기본값으로 NOW()/CURRENT_TIMESTAMP를 쓰지 않는다(앱이 넣는다).
--   - enum 칸은 VARCHAR. CHECK는 런북이 손으로 쓰는 member.status, member_role.role에만.
--   - 제약에는 이름을 붙인다(uk_ 유일, ck_ 검사, fk_ 외래 키, ix_ 색인).
-- 이 파일은 병합하면 고치지 않는다. 바꿀 것은 V3부터 새 파일로(ERD 7번 Flyway 규칙).

-- 2-1 회원
CREATE TABLE member (
    id            BIGINT      NOT NULL AUTO_INCREMENT,
    nickname      VARCHAR(20) NOT NULL,
    status        VARCHAR(20) NOT NULL,
    terms_version VARCHAR(20) NOT NULL,
    created_at    DATETIME(6) NOT NULL,
    updated_at    DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT ck_member_status CHECK (status IN ('ACTIVE', 'SUSPENDED'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- 2-2 이메일 로그인 정보 (회원 1 : 0..1, 회원을 지우면 함께 지움)
CREATE TABLE email_login (
    member_id     BIGINT       NOT NULL,
    email         VARCHAR(254) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    verified_at   DATETIME(6)  NOT NULL,
    PRIMARY KEY (member_id),
    CONSTRAINT uk_email_login_email UNIQUE (email),
    CONSTRAINT fk_email_login_member FOREIGN KEY (member_id) REFERENCES member (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- 2-3 역할 (회원 1 : N, 회원을 지우면 함께 지움)
CREATE TABLE member_role (
    member_id BIGINT      NOT NULL,
    role      VARCHAR(20) NOT NULL,
    PRIMARY KEY (member_id, role),
    CONSTRAINT fk_member_role_member FOREIGN KEY (member_id) REFERENCES member (id) ON DELETE CASCADE,
    CONSTRAINT ck_member_role_role CHECK (role IN ('CREATOR', 'ADMIN'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- 2-4 제작자 프로필 (회원 1 : 0..1, 프로필이 있으면 회원을 지우지 못함)
CREATE TABLE creator (
    id        BIGINT       NOT NULL AUTO_INCREMENT,
    member_id BIGINT       NOT NULL,
    name      VARCHAR(50)  NOT NULL,
    bio       VARCHAR(500) NULL,
    official  BOOLEAN      NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_creator_member UNIQUE (member_id),
    CONSTRAINT fk_creator_member FOREIGN KEY (member_id) REFERENCES member (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- 3-1 템플릿
CREATE TABLE template (
    id                 BIGINT        NOT NULL AUTO_INCREMENT,
    creator_id         BIGINT        NOT NULL,
    title              VARCHAR(100)  NOT NULL,
    description        VARCHAR(2000) NOT NULL,
    format             VARCHAR(20)   NOT NULL,
    category           VARCHAR(30)   NOT NULL,
    access_type        VARCHAR(20)   NOT NULL DEFAULT 'GENERAL',
    status             VARCHAR(20)   NOT NULL,
    first_published_at DATETIME(6)   NULL,
    file_key           VARCHAR(100)  NULL,
    file_size          BIGINT        NULL,
    file_content_type  VARCHAR(100)  NULL,
    thumbnail_key      VARCHAR(100)  NULL,
    link_url           VARCHAR(500)  NULL,
    created_at         DATETIME(6)   NOT NULL,
    updated_at         DATETIME(6)   NOT NULL,
    PRIMARY KEY (id),
    KEY ix_template_list (status, first_published_at, id),
    KEY ix_template_creator (creator_id),
    CONSTRAINT fk_template_creator FOREIGN KEY (creator_id) REFERENCES creator (id),
    -- 파일과 링크를 동시에 갖지 않는다. 어떤 형식이 파일이고 어떤 형식이 링크인지는 코드의 enum이 안다
    -- (형식 이름을 여기에 적지 않아, 형식을 더해도 표를 고치지 않는다. ERD 3-1)
    CONSTRAINT ck_template_file_or_link CHECK (file_key IS NULL OR link_url IS NULL),
    -- 파일 칸 셋(키·크기·콘텐츠 타입)은 함께 비거나 함께 찬다
    CONSTRAINT ck_template_file_together CHECK (
        (file_key IS NULL) = (file_size IS NULL)
        AND (file_key IS NULL) = (file_content_type IS NULL)
    ),
    -- 공개 전(DRAFT) ⇔ 처음 공개한 시각이 비어 있음
    CONSTRAINT ck_template_published_at CHECK ((status = 'DRAFT') = (first_published_at IS NULL))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- 3-2 받은 수 (템플릿 1 : 1). 늘릴 때는 UPDATE ... SET download_count = download_count + 1 한 가지로만
CREATE TABLE template_stat (
    template_id    BIGINT NOT NULL,
    download_count BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (template_id),
    CONSTRAINT fk_template_stat_template FOREIGN KEY (template_id) REFERENCES template (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- 3-3 드롭 (템플릿 1 : 0..N, 같은 때에 열린 것은 하나 → 서버가 검사)
-- ends_at, quantity가 둘 다 비면 상시 드롭. NULL이면 CHECK는 통과한다.
CREATE TABLE drops (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    template_id BIGINT      NOT NULL,
    opens_at    DATETIME(6) NOT NULL,
    ends_at     DATETIME(6) NULL,
    quantity    INT         NULL,
    created_at  DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    KEY ix_drops_template (template_id, opens_at),
    CONSTRAINT fk_drops_template FOREIGN KEY (template_id) REFERENCES template (id),
    CONSTRAINT ck_drops_period CHECK (ends_at > opens_at),
    CONSTRAINT ck_drops_quantity CHECK (quantity > 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- 4-1 주문 (회원을 지우면 회원 칸만 비움 = 익명 주문)
-- uk_orders_member_template: 같은 회원 + 같은 템플릿은 한 번. NULL끼리는 겹쳐도 됨(익명 주문 여럿 가능)
CREATE TABLE orders (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    member_id   BIGINT      NULL,
    template_id BIGINT      NOT NULL,
    drop_id     BIGINT      NULL,
    amount      INT         NOT NULL DEFAULT 0,
    status      VARCHAR(20) NOT NULL,
    created_at  DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_orders_member_template UNIQUE (member_id, template_id),
    KEY ix_orders_template (template_id),
    KEY ix_orders_drop (drop_id),
    CONSTRAINT fk_orders_member FOREIGN KEY (member_id) REFERENCES member (id) ON DELETE SET NULL,
    CONSTRAINT fk_orders_template FOREIGN KEY (template_id) REFERENCES template (id),
    CONSTRAINT fk_orders_drop FOREIGN KEY (drop_id) REFERENCES drops (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;
