-- Utilities/Extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE SEQUENCE IF NOT EXISTS public.seq_account;
CREATE TABLE IF NOT EXISTS public.account
(
    id                    bigint                                              NOT NULL,
    last_login            timestamp with time zone                            NOT NULL,
    legacy_asshole_points integer                                             NOT NULL,
    display_name          character varying(255) COLLATE pg_catalog."default" NOT NULL,
    username              character varying(255) COLLATE pg_catalog."default" NOT NULL,
    access_role           character varying(255) COLLATE pg_catalog."default" NOT NULL DEFAULT 'PLAYER'::character varying,
    last_ip               integer,
    asshole_points        integer                                             NOT NULL DEFAULT 0,
    password              character varying(255) COLLATE pg_catalog."default",
    created_on            timestamp with time zone                            NOT NULL DEFAULT now(),
    guest                 boolean                                             NOT NULL DEFAULT true,
    uuid                  uuid                                                NOT NULL DEFAULT uuid_generate_v4(),
    CONSTRAINT account_pkey PRIMARY KEY (id),
    CONSTRAINT idx_username UNIQUE (username),
    CONSTRAINT uk_uuid UNIQUE (uuid)
);

CREATE TABLE IF NOT EXISTS public.account_achievements
(
    id                     bigint  NOT NULL,
    pressed_asshole_button boolean NOT NULL DEFAULT false,
    CONSTRAINT account_achievements_pkey PRIMARY KEY (id),
    CONSTRAINT fk_achievements_account FOREIGN KEY (id)
        REFERENCES public.account (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE SEQUENCE IF NOT EXISTS public.seq_chat;
CREATE TABLE IF NOT EXISTS public.chat
(
    id       bigint                                              NOT NULL,
    "number" integer                                             NOT NULL,
    uuid     uuid                                                NOT NULL,
    type     character varying(255) COLLATE pg_catalog."default" NOT NULL DEFAULT 'LADDER'::character varying,
    CONSTRAINT chat_pkey PRIMARY KEY (id),
    CONSTRAINT uk_type_number UNIQUE (type, "number")
);

CREATE SEQUENCE IF NOT EXISTS public.seq_message;
CREATE TABLE IF NOT EXISTS public.message
(
    id         bigint                                              NOT NULL,
    created_on timestamp with time zone                            NOT NULL,
    message    character varying(512) COLLATE pg_catalog."default" NOT NULL,
    metadata   character varying(512) COLLATE pg_catalog."default",
    uuid       uuid                                                NOT NULL,
    account_id bigint                                              NOT NULL,
    chat_id    bigint                                              NOT NULL,
    deleted_on timestamp with time zone,
    CONSTRAINT message_pkey PRIMARY KEY (id),
    CONSTRAINT fk_message_account FOREIGN KEY (account_id)
        REFERENCES public.account (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT fk_message_chat FOREIGN KEY (chat_id)
        REFERENCES public.chat (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE SEQUENCE IF NOT EXISTS public.seq_round;
CREATE TABLE IF NOT EXISTS public.round
(
    id                                bigint                   NOT NULL,
    base_asshole_ladder               integer                  NOT NULL,
    base_points_requirement           numeric(1000, 0)         NOT NULL,
    created_on                        timestamp with time zone NOT NULL,
    highest_asshole_count             integer                  NOT NULL,
    "number"                          integer                  NOT NULL,
    percentage_of_additional_assholes real                     NOT NULL,
    uuid                              uuid                     NOT NULL,
    closed_on                         timestamp with time zone,
    CONSTRAINT round_pkey PRIMARY KEY (id),
    CONSTRAINT uk_number UNIQUE ("number")
);

CREATE TABLE IF NOT EXISTS public.round_type
(
    round_entity_id bigint NOT NULL,
    types           character varying(255) COLLATE pg_catalog."default",
    CONSTRAINT fk_round_type_round FOREIGN KEY (round_entity_id)
        REFERENCES public.round (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE SEQUENCE IF NOT EXISTS public.seq_game;
CREATE TABLE IF NOT EXISTS public.game
(
    id               bigint NOT NULL,
    uuid             uuid   NOT NULL,
    current_round_id bigint,
    CONSTRAINT game_pkey PRIMARY KEY (id),
    CONSTRAINT fk_game_round FOREIGN KEY (current_round_id)
        REFERENCES public.round (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE SEQUENCE IF NOT EXISTS public.seq_ladder;
CREATE TABLE IF NOT EXISTS public.ladder
(
    id                     bigint                   NOT NULL,
    base_points_to_promote numeric(1000, 0)         NOT NULL,
    "number"               integer                  NOT NULL,
    uuid                   uuid                     NOT NULL,
    round_id               bigint                   NOT NULL,
    created_on             timestamp with time zone NOT NULL,
    scaling                integer                  NOT NULL,
    CONSTRAINT ladder_pkey PRIMARY KEY (id),
    CONSTRAINT uk_number_round UNIQUE ("number", round_id),
    CONSTRAINT fk_ladder_round FOREIGN KEY (round_id)
        REFERENCES public.round (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE TABLE IF NOT EXISTS public.ladder_type
(
    ladder_entity_id bigint NOT NULL,
    types            character varying(255) COLLATE pg_catalog."default",
    CONSTRAINT fk_ladder_type_ladder FOREIGN KEY (ladder_entity_id)
        REFERENCES public.ladder (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE SEQUENCE IF NOT EXISTS public.seq_ranker;
CREATE TABLE IF NOT EXISTS public.ranker
(
    id           bigint           NOT NULL,
    auto_promote boolean,
    bias         integer          NOT NULL,
    grapes       numeric(1000, 0) NOT NULL,
    growing      boolean,
    multiplier   integer          NOT NULL,
    points       numeric(1000, 0) NOT NULL,
    power        numeric(1000, 0) NOT NULL,
    rank         integer          NOT NULL,
    uuid         uuid             NOT NULL,
    vinegar      numeric(1000, 0) NOT NULL,
    account_id   bigint           NOT NULL,
    ladder_id    bigint           NOT NULL,
    CONSTRAINT ranker_pkey PRIMARY KEY (id),
    CONSTRAINT uk_account_ladder UNIQUE (account_id, ladder_id),
    CONSTRAINT fk_ranker_account FOREIGN KEY (account_id)
        REFERENCES public.account (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT fk_ranker_ladder FOREIGN KEY (ladder_id)
        REFERENCES public.ladder (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE TABLE IF NOT EXISTS public.ranker_unlocks
(
    id                          bigint  NOT NULL,
    auto_promote                boolean NOT NULL,
    pressed_asshole_button      boolean NOT NULL,
    reached_asshole_ladder      boolean NOT NULL,
    reached_base_asshole_ladder boolean NOT NULL,
    CONSTRAINT ranker_unlocks_pkey PRIMARY KEY (id),
    CONSTRAINT fk_unlocks_ranker FOREIGN KEY (id)
        REFERENCES public.ranker (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);