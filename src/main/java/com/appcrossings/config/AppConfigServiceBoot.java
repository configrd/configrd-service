package com.appcrossings.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication(scanBasePackages={"com.appcrossings.config"})
@PropertySource("classpath:application.properties")
public class AppConfigServiceBoot {

  public static void main(String[] args) {
    SpringApplication.run(AppConfigServiceBoot.class, args);
  }

}
