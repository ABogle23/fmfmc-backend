package com.icl.fmfmc_backend.config;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.sql.SQLException;

@TestConfiguration
@Testcontainers
public class TestContainerConfig {

  @Container
  public static MySQLContainer<?> mysql =
      new MySQLContainer<>("mysql:8.3.0")
          .withDatabaseName("fmfmc_db_container")
          .withUsername("root")
          .withPassword("Mypassword23!");

  @DynamicPropertySource
  static void mysqlProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", mysql::getJdbcUrl);
    registry.add("spring.datasource.username", mysql::getUsername);
    registry.add("spring.datasource.password", mysql::getPassword);
    registry.add(
        "spring.jpa.properties.hibernate.dialect",
        () -> "org.hibernate.spatial.dialect.mysql.MySQLSpatialDialect");
    registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
  }

  public void loadData(DataSource dataSource, String sqlFile) {
    Resource resource = new ClassPathResource(sqlFile);
    ResourceDatabasePopulator databasePopulator = new ResourceDatabasePopulator(resource);
    databasePopulator.execute(dataSource);
  }

  @Bean
  public DataSource dataSource() {
    DriverManagerDataSource dataSource = new DriverManagerDataSource();
    dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
    dataSource.setUrl(mysql.getJdbcUrl());
    dataSource.setUsername(mysql.getUsername());
    dataSource.setPassword(mysql.getPassword());
    return dataSource;
  }

  @PostConstruct
  public void startContainer() {
    System.out.println("Starting MySQL container...");
    mysql.start();
    System.out.println("MySQL container started.");
  }

  //    @Bean
  //    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
  //        return new JdbcTemplate(dataSource);
  //    }

  public void clearTables(JdbcTemplate jdbcTemplate) {

    String url = null;
    try {
      url = jdbcTemplate.getDataSource().getConnection().getMetaData().getURL();
      System.out.println("Datasource URL: " + url);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    if (!url.contains("container") && !url.contains("3306")) {
      throw new IllegalStateException(
          "Attempting to clear tables on a non-test database URL: " + url);
    }

    //        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0;");
    jdbcTemplate.update("DELETE FROM charger_connections;");
    jdbcTemplate.update("DELETE FROM chargers;");
    jdbcTemplate.update("DELETE FROM address_info;");
    //        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1;");

  }

  @Deprecated
  public void createIndexes(JdbcTemplate jdbcTemplate) {
    // Create index on `numberOfPoints` in the `chargers` table
    jdbcTemplate.execute("CREATE INDEX idx_number_of_points ON chargers(number_of_points);");
    jdbcTemplate.execute("CREATE INDEX idx_location ON chargers(location);");
    // Create index on `powerKW` in the `connections` table (assuming connections is a separate table and not embedded)
    jdbcTemplate.execute("CREATE INDEX idx_power_kw ON charger_connections(powerKW);");
    // Create index on `connectionTypeID` in the `connections` table
    jdbcTemplate.execute("CREATE INDEX idx_connection_type_id ON charger_connections(connection_typeid);");

    System.out.println("Indexes created.");
  }

}
