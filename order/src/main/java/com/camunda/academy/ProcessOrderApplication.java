package com.camunda.academy;

import io.camunda.client.annotation.Deployment;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Deployment(resources = "classpath:diagram_1.bpmn")
public class ProcessOrderApplication {

  public static void main(String[] args) {
    SpringApplication.run(ProcessOrderApplication.class, args);
  }
}
