--

ALTER TABLE public.account
    ADD COLUMN is_asshole boolean NOT NULL DEFAULT 'false';

ALTER TABLE public.account
    ADD COLUMN last_login timestamp without time zone default '2022-01-05 02:16:08.264337';

ALTER TABLE public.account
    ADD COLUMN was_asshole boolean NOT NULL DEFAULT 'false';

ALTER TABLE IF EXISTS public.ladder DROP COLUMN IF EXISTS past_ladder_id;
ALTER TABLE IF EXISTS public.ladder DROP COLUMN IF EXISTS next_ladder_id;

--

ALTER TABLE public.account
    ADD COLUMN times_asshole boolean NOT NULL DEFAULT '0';

ALTER TABLE IF EXISTS public.account DROP COLUMN IF EXISTS was_asshole;


