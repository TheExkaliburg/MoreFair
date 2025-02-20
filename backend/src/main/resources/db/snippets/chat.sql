-- Showing All Messages from Account
SELECT *
FROM message m
         INNER JOIN account a ON m.account_id = a.id
WHERE a.id IN (61362, 137499)
ORDER by m.created_on DESC;

-- Deleting All Messages from Account
DELETE
FROM message m
    USING account a
WHERE m.account_id = a.id
  AND a.id IN (61362, 137499);