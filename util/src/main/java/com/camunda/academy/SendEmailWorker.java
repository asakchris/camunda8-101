package com.camunda.academy;

import io.camunda.client.annotation.JobWorker;
import io.camunda.client.annotation.Variable;
import io.camunda.client.api.response.ActivatedJob;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SendEmailWorker {
  private static final Logger LOG = LoggerFactory.getLogger(SendEmailWorker.class);

  @JobWorker(type = "send-email")
  public void sendEmail(final ActivatedJob job,
      @Variable(name = "name") @Nullable String name) {
    LOG.info("Processing send-email job: {}", job.getKey());

    String userName = name != null ? name : "User";
    LOG.info("Email sent to the user with name: {}", userName);

    LOG.info("send-email job completed: {}", job.getKey());
  }
}

