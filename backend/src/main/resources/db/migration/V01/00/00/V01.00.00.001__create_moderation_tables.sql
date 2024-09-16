CREATE SEQUENCE IF NOT EXISTS public.seq_name_change;
CREATE TABLE IF NOT EXISTS public.name_change
(
    id           bigint                   NOT NULL DEFAULT nextval('public.seq_name_change'),
    account_id   bigint                   NOT NULL,
    created_on   timestamp with time zone NOT NULL DEFAULT now(),
    display_name character varying(255)   NOT NULL,
    CONSTRAINT name_change_pk PRIMARY KEY (id),
    CONSTRAINt name_change_fk_account FOREIGN KEY (account_id) REFERENCES public.account (id)
);

CREATE SEQUENCE IF NOT EXISTS public.seq_user_event;
CREATE TABLE IF NOT EXISTS public.user_event
(
    id         bigint                   NOT NULL DEFAULT nextval('public.seq_user_event'),
    account_id bigint                   NOT NULL,
    created_on timestamp with time zone NOT NULL DEFAULT now(),
    event_type character varying(63)    NOT NULL,
    is_trusted boolean                  NOT NULL,
    screen_x   int                      NOT NULL,
    screen_y   int                      NOT NULL,
    CONSTRAINT user_event_pk PRIMARY KEY (id),
    CONSTRAINT user_event_fk_account FOREIGN KEY (account_id) REFERENCES public.account (id)
);