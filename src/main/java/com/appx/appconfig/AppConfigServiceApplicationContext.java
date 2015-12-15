package com.appx.appconfig;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@SpringBootApplication
@PropertySource("classpath:application.properties")
public class AppConfigServiceApplicationContext {

  public static void main(String[] args) {
    SpringApplication.run(AppConfigServiceApplicationContext.class, args);
  }
  
  public static PropertySourcesPlaceholderConfigurer buildConfigs(){
    return new PropertySourcesPlaceholderConfigurer();
  }
}
