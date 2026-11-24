package com.example.demo;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.JdbcClient;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:postgresql://localhost:5432/poultry_crm",
        "spring.datasource.driver-class-name=org.postgresql.Driver",
        "spring.datasource.username=postgres",
        "spring.datasource.password=postgres",
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.jpa.properties.hibernate.default_schema=demo",
        "spring.flyway.enabled=true",
        "spring.flyway.schemas=demo",
        "spring.flyway.default-schema=demo",
        "spring.flyway.baseline-on-migrate=false",
        "reports.worker.enabled=false",
        "server.port=0"
})
class FlywayMigrationSmokeTest {
    @Autowired
    private JdbcClient jdbc;

    @Autowired
    private DataSource dataSource;

    @Test
    void freshInstallAndExistingSchemaBaselineBothApplyTenantIsolation() {
        assertTenantSchemaExists();

        // Simulate the pre-Flyway production schema: V1 tables exist, but no
        // tenant discriminator columns, tenant table, generic data table or
        // Flyway history table exist yet.
        jdbc.sql("DROP TABLE IF EXISTS demo.business_record").update();
        jdbc.sql("DROP TABLE IF EXISTS demo.tenant").update();
        String[] tenantTables = {
                "user_account", "buying_party", "delievery", "delievery_party_relation",
                "employee", "expenses", "finance_order_settlement", "finance_payment_allocation",
                "mortality", "\"order\"", "order_type", "party_order_relation", "payment",
                "selling_party", "supplier_pickup", "trip", "trip_party_relation", "vehicle",
                "vendor_delievery", "generated_report"
        };
        for (String table : tenantTables) {
            jdbc.sql("ALTER TABLE demo." + table + " DROP COLUMN IF EXISTS tenant_id").update();
        }
        jdbc.sql("DROP TABLE IF EXISTS demo.flyway_schema_history").update();

        Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .schemas("demo")
                .defaultSchema("demo")
                .baselineOnMigrate(true)
                .baselineVersion("1")
                .load()
                .migrate();

        assertTenantSchemaExists();
        assertThat(jdbc.sql("""
                select count(*) from demo.flyway_schema_history
                 where type = 'BASELINE' and version = '1'
                """).query(Long.class).single()).isEqualTo(1L);
    }

    private void assertTenantSchemaExists() {
        assertThat(jdbc.sql("select count(*) from demo.tenant")
                .query(Long.class).single()).isEqualTo(1L);
        assertThat(jdbc.sql("""
                select count(*) from information_schema.columns
                 where table_schema = 'demo'
                   and table_name = 'employee'
                   and column_name = 'tenant_id'
                """).query(Long.class).single()).isEqualTo(1L);
        assertThat(jdbc.sql("""
                select count(*) from information_schema.tables
                 where table_schema = 'demo'
                   and table_name = 'business_record'
                """).query(Long.class).single()).isEqualTo(1L);
    }
}
