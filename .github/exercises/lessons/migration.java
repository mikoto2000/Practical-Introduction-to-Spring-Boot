package dev.mikoto2000.springboot.flyway;

import static org.junit.jupiter.api.Assertions.*;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:migrations;DB_CLOSE_DELAY=-1",
    "spring.h2.console.enabled=false"
})
class MigrationTest {
  @Autowired Flyway flyway;
  @Autowired JdbcTemplate jdbc;

  @Test void appliesBothMigrationsAndPreservesRowsWhenRunAgain() {
    assertEquals(2, flyway.info().applied().length);
    assertEquals(0, flyway.info().pending().length);
    jdbc.update("INSERT INTO ACCOUNT (name, password, role) VALUES (?, ?, ?)", "alice", "example", "USER");
    assertEquals(0, flyway.migrate().migrationsExecuted);
    flyway.validate();
    assertEquals("USER", jdbc.queryForObject("SELECT role FROM ACCOUNT WHERE name='alice'", String.class));
    assertEquals(2, jdbc.queryForObject("SELECT COUNT(*) FROM \"flyway_schema_history\" WHERE \"version\" IS NOT NULL", Integer.class));
  }
}
