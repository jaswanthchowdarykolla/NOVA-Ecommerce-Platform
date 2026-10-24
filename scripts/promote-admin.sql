-- 1) Register an account through /register first.
-- 2) Replace the email below with that account's exact email.
-- 3) Run this in the NOVA PostgreSQL database, then sign out and sign back in.
UPDATE users SET role = 'ADMIN' WHERE email = 'replace-with-your-account-email@example.com';
