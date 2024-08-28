-- Utilities/Extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Account
CREATE SEQUENCE IF NOT EXISTS public.seq_account;
CREATE TABLE IF NOT EXISTS public.account
(
    id                    bigint                   NOT NULL DEFAULT nextval('public.seq_account'),
    uuid                  uuid                     NOT NULL DEFAULT uuid_generate_v4(),
    access_role           character varying(255)   NOT NULL,
    created_on            timestamp with time zone NOT NULL DEFAULT now(),
    display_name          character varying(255)   NOT NULL,
    guest                 boolean                  NOT NULL,
    last_ip               integer,
    last_login            timestamp with time zone NOT NULL,
    legacy_asshole_points integer                  NOT NULL,
    password              character varying(255)   NOT NULL,
    username              character varying(255)   NOT NULL,
    CONSTRAINT account_pkey PRIMARY KEY (id),
    CONSTRAINT account_uk_uuid UNIQUE (uuid),
    CONSTRAINT account_uk_username UNIQUE (username)
);

-- Account Settings
CREATE SEQUENCE IF NOT EXISTS public.seq_account_settings;
CREATE TABLE IF NOT EXISTS public.account_settings
(
    id            bigint  NOT NULL DEFAULT nextval('public.seq_account_settings'),
    uuid          uuid    NOT NULL DEFAULT uuid_generate_v4(),
    account_id    bigint  NOT NULL,
    vinegar_split integer NOT NULL DEFAULT 50,
    CONSTRAINT account_settings_pkey PRIMARY KEY (id),
    CONSTRAINT account_settings_uuid UNIQUE (uuid),
    CONSTRAINT account_settings_account UNIQUE (account_id),
    FOREIGN KEY (account_id) REFERENCES public.account (id) MATCH FULL
);

-- Chat
CREATE SEQUENCE IF NOT EXISTS public.seq_chat;
CREATE TABLE IF NOT EXISTS public.chat
(
    id     bigint                 NOT NULL DEFAULT nextval('public.seq_chat'),
    uuid   uuid                   NOT NULL DEFAULT uuid_generate_v4(),
    number integer,
    type   character varying(255) NOT NULL,
    CONSTRAINT chat_pkey PRIMARY KEY (id),
    CONSTRAINT chat_uk_uuid UNIQUE (uuid),
    CONSTRAINT chat_uk_type_number UNIQUE (type, number)
);

-- Message
CREATE SEQUENCE IF NOT EXISTS public.seq_message;
CREATE TABLE IF NOT EXISTS public.message
(
    id         bigint                                              NOT NULL DEFAULT nextval('public.seq_message'),
    uuid       uuid                                                NOT NULL DEFAULT uuid_generate_v4(),
    account_id bigint                                              NOT NULL,
    chat_id    bigint                                              NOT NULL,
    created_on timestamp with time zone                            NOT NULL DEFAULT now(),
    deleted_on timestamp with time zone,
    message    character varying(512) COLLATE pg_catalog."default" NOT NULL,
    metadata   character varying(512) COLLATE pg_catalog."default",
    CONSTRAINT message_pkey PRIMARY KEY (id),
    CONSTRAINT message_uk_uuid UNIQUE (uuid),
    FOREIGN KEY (account_id) REFERENCES public.account (id) MATCH FULL,
    FOREIGN KEY (chat_id) REFERENCES public.chat (id) MATCH FULL
);

-- Season
CREATE SEQUENCE IF NOT EXISTS public.seq_season;
CREATE TABLE IF NOT EXISTS public.season
(
    id         bigint                                              NOT NULL DEFAULT nextval('public.seq_season'),
    uuid       uuid                                                NOT NULL DEFAULT uuid_generate_v4(),
    number     integer                                             NOT NULL,
    created_on timestamp with time zone                            NOT NULL DEFAULT now(),
    closed_on  timestamp with time zone,
    end_type   character varying(255) COLLATE pg_catalog."default" NOT NULL DEFAULT 'MANUAL',
    CONSTRAINT season_pkey PRIMARY KEY (id),
    CONSTRAINT season_uk_uuid UNIQUE (uuid),
    CONSTRAINT season_uk_number UNIQUE (number)
);

CREATE TABLE IF NOT EXISTS public.season_type
(
    season_entity_id bigint                                              NOT NULL,
    types            character varying(255) COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT season_type_fk_season FOREIGN KEY (season_entity_id)
        REFERENCES public.season (id) MATCH FULL
);

-- Achievements
CREATE SEQUENCE IF NOT EXISTS public.seq_achievements;
CREATE TABLE IF NOT EXISTS public.achievements
(
    id                      bigint  NOT NULL DEFAULT nextval('public.seq_achievements'),
    uuid                    uuid    NOT NULL DEFAULT uuid_generate_v4(),
    account_id              bigint  NOT NULL,
    season_id               bigint  NOT NULL,
    asshole_points          integer NOT NULL,
    pressed_asshole_buttons integer NOT NULL,
    CONSTRAINT achievements_pkey PRIMARY KEY (id),
    CONSTRAINT achievements_uk_uuid UNIQUE (uuid),
    CONSTRAINT achievements_uk_account_season UNIQUE (account_id, season_id),
    FOREIGN KEY (account_id) REFERENCES public.account (id) MATCH FULL,
    FOREIGN KEY (season_id) REFERENCES public.season (id) MATCH FULL
);


-- Round
CREATE SEQUENCE IF NOT EXISTS public.seq_round;
CREATE TABLE IF NOT EXISTS public.round
(
    id                                bigint                   NOT NULL DEFAULT nextval('public.seq_round'),
    uuid                              uuid                     NOT NULL DEFAULT uuid_generate_v4(),
    season_id                         bigint                   NOT NULL,
    number                            integer                  NOT NULL,
    created_on                        timestamp with time zone NOT NULL DEFAULT now(),
    closed_on                         timestamp with time zone,
    asshole_ladder_number             integer                  NOT NULL,
    base_points_requirement           numeric(1000, 0)         NOT NULL,
    percentage_of_additional_assholes real                     NOT NULL,
    CONSTRAINT round_pkey PRIMARY KEY (id),
    CONSTRAINT round_uk_uuid UNIQUE (uuid),
    CONSTRAINT round_uk_season_number UNIQUE (season_id, number),
    FOREIGN KEY (season_id) REFERENCES public.season (id) MATCH FULL
);

CREATE TABLE IF NOT EXISTS public.round_type
(
    round_entity_id bigint                                              NOT NULL,
    types           character varying(255) COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT round_type_fk_round FOREIGN KEY (round_entity_id)
        REFERENCES public.round (id) MATCH FULL
);

-- Unlocks
CREATE SEQUENCE IF NOT EXISTS public.seq_unlocks;
CREATE TABLE IF NOT EXISTS public.unlocks
(
    id                     bigint  NOT NULL DEFAULT nextval('public.seq_unlocks'),
    uuid                   uuid    NOT NULL DEFAULT uuid_generate_v4(),
    account_id             bigint  NOT NULL,
    round_id               bigint  NOT NULL,
    pressed_asshole_button boolean NOT NULL,
    reached_asshole_ladder boolean NOT NULL,
    CONSTRAINT unlocks_pkey PRIMARY KEY (id),
    CONSTRAINT unlocks_uk_uuid UNIQUE (uuid),
    CONSTRAINT unlocks_uk_account_round UNIQUE (account_id, round_id),
    FOREIGN KEY (account_id) REFERENCES public.account (id) MATCH FULL,
    FOREIGN KEY (round_id) REFERENCES public.round (id) MATCH FULL
);

-- Ladder
CREATE SEQUENCE IF NOT EXISTS public.seq_ladder;
CREATE TABLE IF NOT EXISTS public.ladder
(
    id                     bigint                   NOT NULL DEFAULT nextval('public.seq_ladder'),
    uuid                   uuid                     NOT NULL DEFAULT uuid_generate_v4(),
    round_id               bigint                   NOT NULL,
    base_points_to_promote numeric(1000, 0)         NOT NULL,
    created_on             timestamp with time zone NOT NULL DEFAULT now(),
    number                 integer                  NOT NULL,
    scaling                integer                  NOT NULL,
    CONSTRAINT ladder_pkey PRIMARY KEY (id),
    CONSTRAINT ladder_uk_uuid UNIQUE (uuid),
    CONSTRAINT ladder_uk_number_round UNIQUE (number, round_id),
    FOREIGN KEY (round_id) REFERENCES public.round (id) MATCH FULL
);

CREATE TABLE IF NOT EXISTS public.ladder_type
(
    ladder_entity_id bigint                                              NOT NULL,
    types            character varying(255) COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT ladder_type_fk_ladder FOREIGN KEY (ladder_entity_id)
        REFERENCES public.ladder (id) MATCH FULL
);

-- Ranker
CREATE SEQUENCE IF NOT EXISTS public.seq_ranker;
CREATE TABLE IF NOT EXISTS public.ranker
(
    id           bigint                   NOT NULL DEFAULT nextval('public.seq_ranker'),
    uuid         uuid                     NOT NULL DEFAULT uuid_generate_v4(),
    account_id   bigint                   NOT NULL,
    ladder_id    bigint                   NOT NULL,
    rank         integer                  NOT NULL,
    multiplier   integer                  NOT NULL,
    bias         integer                  NOT NULL,
    power        numeric(1000, 0)         NOT NULL,
    points       numeric(1000, 0)         NOT NULL,
    grapes       numeric(1000, 0)         NOT NULL,
    vinegar      numeric(1000, 0)         NOT NULL,
    wine         numeric(1000, 0)         NOT NULL,
    auto_promote boolean                  NOT NULL,
    created_on   timestamp with time zone NOT NULL DEFAULT now(),
    promoted_on  timestamp with time zone,
    CONSTRAINT ranker_pkey PRIMARY KEY (id),
    CONSTRAINT ranker_uk_uuid UNIQUE (uuid),
    CONSTRAINT ranker_uk_account_ladder UNIQUE (account_id, ladder_id),
    FOREIGN KEY (account_id) REFERENCES public.account (id) MATCH SIMPLE,
    FOREIGN KEY (ladder_id) REFERENCES public.ladder (id) MATCH SIMPLE
);

--Vinegar Throws
CREATE SEQUENCE IF NOT EXISTS public.seq_vinegar_throw;
CREATE TABLE IF NOT EXISTS public.vinegar_throw
(
    id                 bigint                                              NOT NULL DEFAULT nextval('public.seq_vinegar_throw'),
    uuid               uuid                                                NOT NULL DEFAULT uuid_generate_v4(),
    timestamp          timestamp with time zone                            NOT NULL DEFAULT now(),
    thrower_account_id bigint                                              NOT NULL,
    target_account_id  bigint                                              NOT NULL,
    ladder_id          bigint                                              NOT NULL,
    vinegar_thrown     numeric(1000, 0)                                    NOT NULL,
    percentage_thrown  integer                                             NOT NULL,
    vinegar_defended   numeric(1000, 0)                                    NOT NULL,
    wine_defended      numeric(1000, 0)                                    NOT NULL,
    success_type       character varying(255) COLLATE pg_catalog."default" NOT NULL,
    stolen_points      integer                                             NOT NULL,
    stolen_streak      integer                                             NOT NULL,
    CONSTRAINT vinegar_throw_pkey PRIMARY KEY (id),
    CONSTRAINT vinegar_throw_uk_uuid UNIQUE (uuid),
    FOREIGN KEY (thrower_account_id) REFERENCES public.account (id) MATCH SIMPLE,
    FOREIGN KEY (target_account_id) REFERENCES public.account (id) MATCH SIMPLE,
    FOREIGN KEY (ladder_id) REFERENCES public.ladder (id) MATCH SIMPLE
)

