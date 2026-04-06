package com.ecommerce.order_service.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.*;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudWatchLogService {

    private static final String LOG_GROUP  = "/order-service/logs";
    private static final String LOG_STREAM = "order-service-stream";

    private final CloudWatchLogsClient cloudWatchLogsClient;

    @PostConstruct
    public void init() {
        createLogGroupIfNotExists();
        createLogStreamIfNotExists();
        log.info("CloudWatch Logs configurado: grupo={}, stream={}", LOG_GROUP, LOG_STREAM);
    }

    public void sendLog(String message) {
        try {
            cloudWatchLogsClient.putLogEvents(PutLogEventsRequest.builder()
                    .logGroupName(LOG_GROUP)
                    .logStreamName(LOG_STREAM)
                    .logEvents(List.of(
                            InputLogEvent.builder()
                                    .message(message)
                                    .timestamp(Instant.now().toEpochMilli())
                                    .build()
                    ))
                    .build());
        } catch (Exception e) {
            log.warn("No se pudo enviar log a CloudWatch: {}", e.getMessage());
        }
    }

    private void createLogGroupIfNotExists() {
        try {
            cloudWatchLogsClient.createLogGroup(
                    CreateLogGroupRequest.builder().logGroupName(LOG_GROUP).build()
            );
        } catch (ResourceAlreadyExistsException ignored) {
        }
    }

    private void createLogStreamIfNotExists() {
        try {
            cloudWatchLogsClient.createLogStream(
                    CreateLogStreamRequest.builder()
                            .logGroupName(LOG_GROUP)
                            .logStreamName(LOG_STREAM)
                            .build()
            );
        } catch (ResourceAlreadyExistsException ignored) {
        }
    }
}