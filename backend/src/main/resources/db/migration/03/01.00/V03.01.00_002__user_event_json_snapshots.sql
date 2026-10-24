ALTER TABLE public.user_event
    ADD COLUMN metadata_version integer,
    ADD COLUMN metadata jsonb,
    ADD COLUMN snapshot_version integer,
    ADD COLUMN snapshot jsonb;

UPDATE public.user_event
SET metadata_version = 1,
    metadata = jsonb_build_object(
        'isTrusted', is_trusted,
        'screenX', screen_x,
        'screenY', screen_y
    );

ALTER TABLE public.user_event
    DROP COLUMN is_trusted,
    DROP COLUMN screen_x,
    DROP COLUMN screen_y,
    ADD CONSTRAINT user_event_metadata_version_check CHECK (
        (metadata IS NULL AND metadata_version IS NULL)
        OR (metadata IS NOT NULL AND metadata_version IS NOT NULL)
    ),
    ADD CONSTRAINT user_event_snapshot_version_check CHECK (
        (snapshot IS NULL AND snapshot_version IS NULL)
        OR (snapshot IS NOT NULL AND snapshot_version IS NOT NULL)
    );
