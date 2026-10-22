ALTER TABLE public.ladder
    RENAME COLUMN created_on TO first_reached_on;

ALTER TABLE public.ladder
    ALTER COLUMN first_reached_on DROP NOT NULL,
    ALTER COLUMN first_reached_on DROP DEFAULT;
