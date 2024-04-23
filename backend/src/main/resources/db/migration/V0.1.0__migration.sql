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

-- Drop Rest of Tables
DROP TABLE IF EXISTS public.account_achievements;
DROP TABLE IF EXISTS public.message;
DROP TABLE IF EXISTS public.chat;
DROP TABLE IF EXISTS public.game;
DROP TABLE IF EXISTS public.ranker_unlocks;
DROP TABLE IF EXISTS public.ranker;
DROP TABLE IF EXISTS public.ladder_type;
DROP TABLE IF EXISTS public.ladder;
DROP TABLE IF EXISTS public.round_type;
DROP TABLE IF EXISTS public.round;

-- Drop All Sequences
DROP SEQUENCE IF EXISTS public.seq_chat;
DROP SEQUENCE IF EXISTS public.seq_game;
DROP SEQUENCE IF EXISTS public.seq_ladder;
DROP SEQUENCE IF EXISTS public.seq_message;
DROP SEQUENCE IF EXISTS public.seq_ranker;
DROP SEQUENCE IF EXISTS public.seq_round;