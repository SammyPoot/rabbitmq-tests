package uk.gov.ons.fwmt.rabbitmq_tests;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

/**
 * Main entry point into the TM Gateway
 *
 * @author Chris Hardman
 */

@Slf4j
@SpringBootApplication
@Configuration
@EnableRetry
public class Application {

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
    log.debug("Started application");
  }
}
