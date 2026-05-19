package com.brmc.account;

import jakarta.annotation.PostConstruct;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Servicio tecnico que ajusta restricciones de esquema creadas por versiones anteriores.
 *
 * <p>Hibernate puede crear columnas nuevas con {@code ddl-auto=update}, pero no siempre actualiza
 * restricciones {@code CHECK} existentes cuando se agregan valores a enums Java. Este componente
 * se ejecuta al iniciar la aplicacion y, solo en PostgreSQL, reemplaza las restricciones de enums
 * que bloquean eventos y estados nuevos. No modifica datos de negocio.</p>
 */
@Component
class SchemaCompatibilityService {

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    /**
     * Crea el servicio de compatibilidad de esquema.
     *
     * @param jdbcTemplate ejecutor SQL de Spring.
     * @param dataSource datasource configurado por la aplicacion.
     */
    SchemaCompatibilityService(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
    }

    /**
     * Actualiza restricciones antiguas de PostgreSQL despues de que Hibernate haya preparado el
     * esquema.
     */
    @PostConstruct
    void updatePostgresEnumChecks() {
        if (!isPostgres()) {
            return;
        }

        refreshSystemEventTypeCheck();
        refreshPaymentAllocationStatusCheck();
        refreshInvoiceStatusCheck();
        relaxProductCodeUniqueConstraint();
    }

    private boolean isPostgres() {
        try (var connection = dataSource.getConnection()) {
            return connection.getMetaData().getDatabaseProductName().toLowerCase().contains("postgresql");
        } catch (Exception exception) {
            return false;
        }
    }

    private void refreshSystemEventTypeCheck() {
        jdbcTemplate.execute("""
                DO $$
                DECLARE
                    constraint_record record;
                BEGIN
                    FOR constraint_record IN
                        SELECT nsp.nspname, rel.relname, con.conname
                        FROM pg_constraint con
                        JOIN pg_class rel ON rel.oid = con.conrelid
                        JOIN pg_namespace nsp ON nsp.oid = rel.relnamespace
                        WHERE rel.relname = 'system_events_t'
                          AND con.contype = 'c'
                          AND pg_get_constraintdef(con.oid) ILIKE '%type%'
                    LOOP
                        EXECUTE format(
                            'ALTER TABLE %I.%I DROP CONSTRAINT %I',
                            constraint_record.nspname,
                            constraint_record.relname,
                            constraint_record.conname
                        );
                    END LOOP;
                END $$;
                """);
        jdbcTemplate.execute("""
                ALTER TABLE system_events_t
                ADD CONSTRAINT system_events_t_type_check
                CHECK (type IN (
                    'ACCOUNT_CREATED',
                    'PAYMENT_RECEIVED',
                    'UNALLOCATED_PAYMENT_CREATED',
                    'REFUND_SENT',
                    'WRITE_OFF_APPLIED',
                    'ACCOUNT_CLOSED',
                    'DISPUTE_CREATED',
                    'DISPUTE_SETTLEMENT_CREATED',
                    'DISPUTE_SETTLED',
                    'DISPUTE_APPROVED',
                    'DISPUTE_REJECTED',
                    'VIRTUAL_TIME_UPDATED',
                    'VIRTUAL_TIME_RESET',
                    'PRODUCT_CREATED',
                    'PRODUCT_UPDATED',
                    'PRODUCT_ACTIVATED',
                    'PRODUCT_DEACTIVATED',
                    'SERVICE_CATALOG_CREATED',
                    'SERVICE_CATALOG_UPDATED',
                    'SERVICE_ACTIVATED',
                    'SERVICE_CREATED',
                    'SERVICE_UPDATED',
                    'SERVICE_SUSPENDED',
                    'SERVICE_REACTIVATED',
                    'SERVICE_TERMINATED',
                    'SERVICE_PRODUCT_ASSIGNED',
                    'SERVICE_PRODUCT_CANCELLED',
                    'BILLING_RUN_STARTED',
                    'BILLING_RUN_COMPLETED',
                    'BILLING_RUN_FAILED',
                    'BILLING_CHARGE_CREATED',
                    'BILLINFO_CREATED',
                    'BILL_CREATED',
                    'ITEM_CREATED',
                    'BILL_ITEM_CREATED',
                    'INVENTORY_ITEM_CREATED',
                    'INVENTORY_ITEM_UPDATED',
                    'INVENTORY_AVAILABILITY_CHANGED',
                    'INVOICE_GENERATED',
                    'INVOICE_LINE_CREATED',
                    'INVOICE_SENT',
                    'INVOICE_CANCELLED',
                    'INVOICE_PAID',
                    'INVOICE_PARTIALLY_PAID',
                    'CREDIT_NOTE_CREATED',
                    'CREDIT_NOTE_LINE_CREATED',
                    'CREDIT_NOTE_APPLIED',
                    'CREDIT_NOTE_CANCELLED'
                ))
                """);
    }

    private void refreshPaymentAllocationStatusCheck() {
        jdbcTemplate.execute("""
                DO $$
                DECLARE
                    constraint_record record;
                BEGIN
                    FOR constraint_record IN
                        SELECT nsp.nspname, rel.relname, con.conname
                        FROM pg_constraint con
                        JOIN pg_class rel ON rel.oid = con.conrelid
                        JOIN pg_namespace nsp ON nsp.oid = rel.relnamespace
                        WHERE rel.relname = 'payments_t'
                          AND con.contype = 'c'
                          AND pg_get_constraintdef(con.oid) ILIKE '%allocation_status%'
                    LOOP
                        EXECUTE format(
                            'ALTER TABLE %I.%I DROP CONSTRAINT %I',
                            constraint_record.nspname,
                            constraint_record.relname,
                            constraint_record.conname
                        );
                    END LOOP;
                END $$;
                """);
        jdbcTemplate.execute("""
                ALTER TABLE payments_t
                ADD CONSTRAINT payments_t_allocation_status_check
                CHECK (
                    allocation_status IS NULL
                    OR allocation_status IN ('UNALLOCATED', 'PARTIALLY_ALLOCATED', 'ALLOCATED')
                )
                """);
    }

    private void refreshInvoiceStatusCheck() {
        jdbcTemplate.execute("""
                DO $$
                DECLARE
                    constraint_record record;
                BEGIN
                    FOR constraint_record IN
                        SELECT nsp.nspname, rel.relname, con.conname
                        FROM pg_constraint con
                        JOIN pg_class rel ON rel.oid = con.conrelid
                        JOIN pg_namespace nsp ON nsp.oid = rel.relnamespace
                        WHERE rel.relname = 'invoices_t'
                          AND con.contype = 'c'
                          AND pg_get_constraintdef(con.oid) ILIKE '%status%'
                    LOOP
                        EXECUTE format(
                            'ALTER TABLE %I.%I DROP CONSTRAINT %I',
                            constraint_record.nspname,
                            constraint_record.relname,
                            constraint_record.conname
                        );
                    END LOOP;
                END $$;
                """);
        jdbcTemplate.execute("""
                ALTER TABLE invoices_t
                ADD CONSTRAINT invoices_t_status_check
                CHECK (status IN (
                    'DRAFT',
                    'ISSUED',
                    'SENT',
                    'PARTIALLY_PAID',
                    'PAID',
                    'PARTIALLY_CREDITED',
                    'CREDITED',
                    'CANCELLED'
                ))
                """);
    }

    private void relaxProductCodeUniqueConstraint() {
        jdbcTemplate.execute("""
                DO $$
                DECLARE
                    constraint_record record;
                BEGIN
                    FOR constraint_record IN
                        SELECT nsp.nspname, rel.relname, con.conname
                        FROM pg_constraint con
                        JOIN pg_class rel ON rel.oid = con.conrelid
                        JOIN pg_namespace nsp ON nsp.oid = rel.relnamespace
                        WHERE rel.relname = 'products_t'
                          AND con.contype = 'u'
                          AND pg_get_constraintdef(con.oid) ILIKE '%(code)%'
                    LOOP
                        EXECUTE format(
                            'ALTER TABLE %I.%I DROP CONSTRAINT %I',
                            constraint_record.nspname,
                            constraint_record.relname,
                            constraint_record.conname
                        );
                    END LOOP;
                END $$;
                """);
    }
}
