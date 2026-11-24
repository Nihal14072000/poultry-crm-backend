package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.JdbcClient;

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

    @Test
    void createsTenantAndTenantScopedBusinessTablesFromAnEmptyDatabase() {
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
