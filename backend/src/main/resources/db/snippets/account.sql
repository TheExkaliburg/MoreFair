-- Creating a new Season

DO
$$
    DECLARE
        var_username char varying(255) = 'd9c3eae7-46c3-4961-965f-889987d2035d';
        var_password char varying(255) = '$argon2id$v=19$m=16384,t=2,p=1$EqgDwmtG5s9/GBqU+zsbTg$qJXUgITyj+d5iXipDpI6zmg3PNyWUI9izYW3xspavgw';
    BEGIN

        INSERT INTO public.account (last_login, legacy_asshole_points, display_name, username,
                                    access_role, password, guest)
        VALUES (now(), 0, 'Mystery Guest', var_username, 'PLAYER', var_password, true);

    END
$$