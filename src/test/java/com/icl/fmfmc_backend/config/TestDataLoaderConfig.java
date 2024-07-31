package com.icl.fmfmc_backend.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;

@TestConfiguration
public class TestDataLoaderConfig {

  @Autowired private TestContainerConfig testContainerConfig;
  @Autowired private DataSource dataSource;

  @Bean
  public TestDataLoader testDataLoader() {
    return new TestDataLoader(testContainerConfig, dataSource);
  }

  public static class TestDataLoader {

    private final TestContainerConfig testContainerConfig;
    private final DataSource dataSource;

    public TestDataLoader(TestContainerConfig testContainerConfig, DataSource dataSource) {
      this.testContainerConfig = testContainerConfig;
      this.dataSource = dataSource;
    }

    @PostConstruct
    public void loadData() {
      testContainerConfig.loadData(dataSource, "build_Test_DB_Data.sql");
    }
  }
}
