ALTER TABLE IF EXISTS public.message
    ADD COLUMN IF NOT EXISTS sent_in_ladder_id bigint;