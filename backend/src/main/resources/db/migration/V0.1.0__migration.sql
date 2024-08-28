-- ### Create Seasons And Achievements
-- FIXME Make sure these are the correct Season And Achievements Schemas
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

-- ### Initialize The First 3 Seasons
DO
$$
    DECLARE
        season_id bigint;
    BEGIN
        SELECT nextval('seq_season') INTO season_id;

        INSERT INTO public.season (id, number, closed_on)
        VALUES (season_id, 1, now());

        INSERT INTO public.season_type (season_entity_id, types)
        VALUES (season_id, 'DEFAULT');
    END
$$;

DO
$$
    DECLARE
        season_id bigint;
    BEGIN
        SELECT nextval('seq_season') INTO season_id;

        INSERT INTO public.season (id, number, closed_on)
        VALUES (season_id, 2, now());

        INSERT INTO public.season_type (season_entity_id, types)
        VALUES (season_id, 'DEFAULT');
    END
$$;

DO
$$
    DECLARE
        season_id bigint;
    BEGIN
        SELECT nextval('seq_season') INTO season_id;

        INSERT INTO public.season (id, number)
        VALUES (season_id, 3);

        INSERT INTO public.season_type (season_entity_id, types)
        VALUES (season_id, 'DEFAULT');
    END
$$;

-- ### Initialize The Achievements/AHPoints in Season 1/2
INSERT INTO public.achievements (account_id, season_id, asshole_points, pressed_asshole_buttons)
SELECT a.id                    AS account_id,
       1                       AS season_id,
       a.legacy_asshole_points AS asshole_points,
       1                       AS pressed_asshole_buttons
FROM public.account a
WHERE a.legacy_asshole_points > 0;

INSERT INTO public.achievements (account_id, season_id, asshole_points, pressed_asshole_buttons)
SELECT a.id                                                  AS account_id,
       2                                                     AS season_id,
       a.asshole_points                                      AS asshole_points,
       CASE WHEN aa.pressed_asshole_button THEN 1 ELSE 0 END AS pressed_asshole_buttons
FROM public.account a
         JOIN public.account_achievements aa on a.id = aa.id
WHERE a.asshole_points > 0
   OR aa.pressed_asshole_button;

-- Update Account Table to new Format
ALTER TABLE IF EXISTS public.account
    DROP COLUMN IF EXISTS asshole_points;
ALTER TABLE IF EXISTS public.account
    ALTER COLUMN access_role DROP DEFAULT;
ALTER TABLE IF EXISTS public.account
    ALTER COLUMN guest DROP DEFAULT;
ALTER TABLE IF EXISTS public.account
    ALTER COLUMN id SET DEFAULT nextval('public.seq_account');
ALTER TABLE IF EXISTS public.account
    ALTER COLUMN password SET NOT NULL;
ALTER TABLE IF EXISTS public.account
    DROP CONSTRAINT idx_username;
ALTER TABLE IF EXISTS public.account
    DROP CONSTRAINT uk_uuid;
ALTER TABLE IF EXISTS public.account
    ADD CONSTRAINT account_uk_username UNIQUE (username);
ALTER TABLE IF EXISTS public.account
    ADD CONSTRAINT account_uk_uuid UNIQUE (uuid);

-- Update Rounds/Ladder/Ranker State:
ALTER TABLE IF EXISTS public.round
    ALTER COLUMN id SET DEFAULT nextval('public.seq_round');
ALTER TABLE IF EXISTS public.round
    ALTER COLUMN uuid SET DEFAULT uuid_generate_v4();
ALTER TABLE IF EXISTS public.round
    ADD COLUMN season_id bigint;
UPDATE public.round
SET season_id = 2;
ALTER TABLE IF EXISTS public.round
    ALTER COLUMN season_id SET NOT NULL;
ALTER TABLE IF EXISTS public.round
    ALTER COLUMN created_on SET DEFAULT now();
ALTER TABLE IF EXISTS public.round
    RENAME COLUMN base_asshole_ladder TO asshole_ladder_number;
UPDATE public.round
SET asshole_ladder_number = asshole_ladder_number + highest_asshole_count;
ALTER TABLE IF EXISTS public.round
    DROP COLUMN highest_asshole_count;
ALTER TABLE IF EXISTS public.round
    DROP CONSTRAINT uk_number;
ALTER TABLE IF EXISTS public.round
    ADD CONSTRAINT round_uk_uuid UNIQUE (uuid);
ALTER TABLE IF EXISTS public.round
    ADD CONSTRAINT round_uk_season_number UNIQUE (season_id, number);
ALTER TABLE IF EXISTS public.round
    ADD FOREIGN KEY (season_id) REFERENCES public.season (id) MATCH FULL;

ALTER TABLE IF EXISTS public.round_type
    ALTER COLUMN types SET NOT NULL;
ALTER TABLE IF EXISTS public.round_type
    DROP CONSTRAINT fk_round_type_round;
ALTER TABLE IF EXISTS public.round_type
    ADD CONSTRAINT round_type_fk_round FOREIGN KEY (round_entity_id)
        REFERENCES public.round (id) MATCH FULL;

ALTER TABLE IF EXISTS public.ladder
    ALTER COLUMN id SET DEFAULT nextval('public.seq_ladder');
ALTER TABLE IF EXISTS public.ladder
    ALTER COLUMN uuid SET DEFAULT uuid_generate_v4();
ALTER TABLE IF EXISTS public.ladder
    ALTER COLUMN created_on SET DEFAULT now();
ALTER TABLE IF EXISTS public.ladder
    DROP CONSTRAINT uk_number_round;
ALTER TABLE IF EXISTS public.ladder
    DROP CONSTRAINT fk_ladder_round;
ALTER TABLE IF EXISTS public.ladder
    ADD CONSTRAINT ladder_uk_uuid UNIQUE (uuid);
ALTER TABLE IF EXISTS public.ladder
    ADD CONSTRAINT ladder_uk_number_round UNIQUE (number, round_id);
ALTER TABLE IF EXISTS public.ladder
    ADD FOREIGN KEY (round_id) REFERENCES public.round (id) MATCH FULL;

ALTER TABLE IF EXISTS public.ladder_type
    ALTER COLUMN types SET NOT NULL;
ALTER TABLE IF EXISTS public.ladder_type
    DROP CONSTRAINT fk_ladder_type_ladder;
ALTER TABLE IF EXISTS public.ladder_type
    ADD CONSTRAINT ladder_type_fk_ladder FOREIGN KEY (ladder_entity_id)
        REFERENCES public.ladder (id) MATCH FULL;

ALTER TABLE IF EXISTS public.ranker
    ALTER COLUMN id SET DEFAULT nextval('public.seq_ranker');
ALTER TABLE IF EXISTS public.ranker
    ALTER COLUMN uuid SET DEFAULT uuid_generate_v4();
ALTER TABLE IF EXISTS public.ranker
    ADD COLUMN IF NOT EXISTS wine numeric(1000, 0);
UPDATE public.ranker
SET wine = 0;
ALTER TABLE IF EXISTS public.ranker
    ALTER COLUMN wine SET NOT NULL;
ALTER TABLE IF EXISTS public.ranker
    ALTER COLUMN auto_promote SET NOT NULL;
ALTER TABLE IF EXISTS public.ranker
    ADD COLUMN IF NOT EXISTS created_on timestamp with time zone NOT NULL DEFAULT now();
ALTER TABLE IF EXISTS public.ranker
    ADD COLUMN IF NOT EXISTS promoted_on timestamp with time zone;
UPDATE public.ranker
SET promoted_on = now()
WHERE growing IS FALSE;
ALTER TABLE IF EXISTS public.ranker
    DROP COLUMN growing;
ALTER TABLE IF EXISTS public.ranker
    DROP CONSTRAINT uk_account_ladder;
ALTER TABLE IF EXISTS public.ranker
    DROP CONSTRAINT fk_ranker_account;
ALTER TABLE IF EXISTS public.ranker
    DROP CONSTRAINT fk_ranker_ladder;
ALTER TABLE IF EXISTS public.ranker
    ADD CONSTRAINT ranker_uk_uuid UNIQUE (uuid);
ALTER TABLE IF EXISTS public.ranker
    ADD CONSTRAINT ranker_uk_account_ladder UNIQUE (account_id, ladder_id);
ALTER TABLE IF EXISTS public.ranker
    ADD FOREIGN KEY (account_id) REFERENCES public.account (id) MATCH SIMPLE;
ALTER TABLE IF EXISTS public.ranker
    ADD FOREIGN KEY (ladder_id) REFERENCES public.ladder (id) MATCH SIMPLE;

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

INSERT INTO public.unlocks (account_id, round_id, pressed_asshole_button, reached_asshole_ladder)
SELECT DISTINCT ON (ro.id, r.account_id) r.account_id              AS account_id,
                                         ro.id                     AS round_id,
                                         ru.pressed_asshole_button AS pressed_asshole_button,
                                         ru.reached_asshole_ladder AS reached_asshole_ladder
FROM public.ranker_unlocks ru
         JOIN public.ranker r on ru.id = r.id
         JOIN public.ladder l on r.ladder_id = l.id
         JOIN public.round ro on l.round_id = ro.id
ORDER BY ro.id, r.account_id, l.number DESC;

DROP TABLE IF EXISTS public.ranker_unlocks;


-- Update Message and Chat
ALTER TABLE IF EXISTS public.chat
    ALTER COLUMN id SET DEFAULT nextval('public.seq_chat'),
    ALTER COLUMN uuid SET DEFAULT uuid_generate_v4(),
    ALTER COLUMN number DROP NOT NULL,
    ALTER COLUMN type TYPE character varying(255),
    ALTER COLUMN type DROP DEFAULT,
    ALTER COLUMN type SET NOT NULL;
ALTER TABLE IF EXISTS public.chat
    DROP CONSTRAINT uk_type_number;
ALTER TABLE IF EXISTS public.chat
    ADD CONSTRAINT chat_uk_uuid UNIQUE (uuid),
    ADD CONSTRAINT chat_uk_type_number UNIQUE (type, number);

ALTER TABLE IF EXISTS public.message
    ALTER COLUMN id SET DEFAULT nextval('public.seq_message'),
    ALTER COLUMN uuid SET DEFAULT uuid_generate_v4(),
    ALTER COLUMN created_on SET DEFAULT now();
ALTER TABLE IF EXISTS public.message
    DROP CONSTRAINT fk_message_account,
    DROP CONSTRAINT fk_message_chat;
ALTER TABLE IF EXISTS public.message
    ADD CONSTRAINT message_uk_uuid UNIQUE (uuid),
    ADD FOREIGN KEY (account_id) REFERENCES public.account (id) MATCH FULL,
    ADD FOREIGN KEY (chat_id) REFERENCES public.chat (id) MATCH FULL;

-- Drop Unneeded Tables
DROP TABLE IF EXISTS public.account_achievements;
DROP TABLE IF EXISTS public.game;

-- Drop All Sequences
DROP SEQUENCE IF EXISTS public.seq_game;