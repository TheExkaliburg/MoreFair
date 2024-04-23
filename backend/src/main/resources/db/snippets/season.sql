-- Creating a new Season

DO
$$
    DECLARE
        season_id bigint;
    BEGIN
        SELECT nextval('seq_season') INTO season_id;

        INSERT INTO public.season (id, number)
        VALUES (season_id, 2);

        INSERT INTO public.season_type (season_entity_id, types)
        VALUES (season_id, 'DEFAULT');
    END
$$