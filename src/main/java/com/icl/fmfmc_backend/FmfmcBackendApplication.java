package com.icl.fmfmc_backend;

import com.icl.fmfmc_backend.service.ChargerUpdateScheduler;
import com.icl.fmfmc_backend.service.EvScraperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import java.util.Scanner;

@SpringBootApplication
public class FmfmcBackendApplication {

  @Autowired private EvScraperService evScraperService;

  @Autowired private ChargerUpdateScheduler chargerUpdateScheduler;

  public static void main(String[] args) {
    SpringApplication.run(FmfmcBackendApplication.class, args);
  }

  //	@EventListener(ApplicationReadyEvent.class)
  //	public void runEvScrapperService() {
  //		evScraperService.scrapeEvData();
  //	}

  /**
   * Listen for user commands.
   *
   */
  @Bean
  @Profile("!test")
  public CommandLineRunner commandLineRunner() {
    return args ->
        new Thread(
                () -> {
                  Scanner scanner = new Scanner(System.in);
                  System.out.println("Enter command (--runScraper or --updateChargers):");
                  while (true) {
                    String input = scanner.nextLine();
                    switch (input) {
                      case "--runScraper":
                        evScraperService.scrapeEvData();
                        break;
                      case "--updateChargers":
                        chargerUpdateScheduler.updateChargers();
                        break;
                      default:
                        System.out.println(
                            "Invalid command. Please use --runScraper or --updateChargers.");
                        break;
                    }
                  }
                })
            .start();
  }
}
