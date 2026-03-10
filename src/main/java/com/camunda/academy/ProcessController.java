package com.camunda.academy;

import io.camunda.client.CamundaClient;
import io.camunda.client.api.response.ProcessInstanceEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/process")
public class ProcessController {

    private static final Logger LOG = LoggerFactory.getLogger(ProcessController.class);

    private final CamundaClient camundaClient;

    public ProcessController(CamundaClient camundaClient) {
        this.camundaClient = camundaClient;
    }

    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> startProcessInstance(
            @RequestBody(required = false) Map<String, Object> variables) {

        if (variables == null) {
            variables = new HashMap<>();
        }

        LOG.info("Starting new process instance of 'process1' with variables: {}", variables);

        ProcessInstanceEvent processInstance = camundaClient.newCreateInstanceCommand()
                .bpmnProcessId("process1")
                .latestVersion()
                .variables(variables)
                .send()
                .join();

        Map<String, Object> response = new HashMap<>();
        response.put("processInstanceKey", processInstance.getProcessInstanceKey());
        response.put("bpmnProcessId", processInstance.getBpmnProcessId());
        response.put("version", processInstance.getVersion());
        response.put("processDefinitionKey", processInstance.getProcessDefinitionKey());

        LOG.info("Process instance started with key: {}", processInstance.getProcessInstanceKey());

        return ResponseEntity.ok(response);
    }
}

