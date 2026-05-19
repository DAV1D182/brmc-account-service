-- BRMC - Migracion para renombrar tablas con sufijo _t
-- Base de datos: brmc_db
--
-- IMPORTANTE:
-- 1. Deten la aplicacion Spring Boot antes de ejecutar este script.
-- 2. Ejecutalo en PostgreSQL sobre la base brmc_db.
-- 3. Hibernate ddl-auto=update NO renombra tablas existentes; por eso se necesita este ALTER TABLE.

-- Verifica tablas actuales
SELECT table_name
FROM information_schema.tables
WHERE table_schema = 'public'
  AND table_name IN (
      'accounts',
      'account_transactions',
      'disputes',
      'payments',
      'refunds',
      'system_events',
      'accounts_t',
      'account_transactions_t',
      'disputes_t',
      'payments_t',
      'refunds_t',
      'system_events_t'
  )
ORDER BY table_name;

-- Renombrar tablas antiguas a nombres nuevos.
-- Estos bloques solo renombran si existe la tabla antigua y todavia no existe la tabla nueva.

DO $$
BEGIN
    IF to_regclass('public.accounts') IS NOT NULL
       AND to_regclass('public.accounts_t') IS NULL THEN
        ALTER TABLE accounts RENAME TO accounts_t;
    END IF;
END $$;

DO $$
BEGIN
    IF to_regclass('public.account_transactions') IS NOT NULL
       AND to_regclass('public.account_transactions_t') IS NULL THEN
        ALTER TABLE account_transactions RENAME TO account_transactions_t;
    END IF;
END $$;

DO $$
BEGIN
    IF to_regclass('public.disputes') IS NOT NULL
       AND to_regclass('public.disputes_t') IS NULL THEN
        ALTER TABLE disputes RENAME TO disputes_t;
    END IF;
END $$;

DO $$
BEGIN
    IF to_regclass('public.payments') IS NOT NULL
       AND to_regclass('public.payments_t') IS NULL THEN
        ALTER TABLE payments RENAME TO payments_t;
    END IF;
END $$;

DO $$
BEGIN
    IF to_regclass('public.refunds') IS NOT NULL
       AND to_regclass('public.refunds_t') IS NULL THEN
        ALTER TABLE refunds RENAME TO refunds_t;
    END IF;
END $$;

DO $$
BEGIN
    IF to_regclass('public.system_events') IS NOT NULL
       AND to_regclass('public.system_events_t') IS NULL THEN
        ALTER TABLE system_events RENAME TO system_events_t;
    END IF;
END $$;

-- Verificacion final
SELECT table_name
FROM information_schema.tables
WHERE table_schema = 'public'
  AND table_name IN (
      'accounts_t',
      'account_transactions_t',
      'disputes_t',
      'payments_t',
      'refunds_t',
      'system_events_t'
  )
ORDER BY table_name;

-- Conteos finales
SELECT 'accounts_t' AS table_name, COUNT(*) AS total FROM accounts_t
UNION ALL
SELECT 'account_transactions_t', COUNT(*) FROM account_transactions_t
UNION ALL
SELECT 'disputes_t', COUNT(*) FROM disputes_t
UNION ALL
SELECT 'payments_t', COUNT(*) FROM payments_t
UNION ALL
SELECT 'refunds_t', COUNT(*) FROM refunds_t
UNION ALL
SELECT 'system_events_t', COUNT(*) FROM system_events_t;

