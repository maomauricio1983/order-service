package com.ecommerce.order_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderEventListener {

    private final SqsClient sqsClient;

    @Value("${aws.sqs.orders-queue-url}")
    private String ordersQueueUrl;

    @Scheduled(fixedDelay = 5000) // cada 5 segundos
    public void pollMessages() {
        List<Message> messages = sqsClient.receiveMessage(
                ReceiveMessageRequest.builder()
                        .queueUrl(ordersQueueUrl)
                        .maxNumberOfMessages(10)
                        .waitTimeSeconds(2) // long polling
                        .build()
        ).messages();

        for (Message message : messages) {
            log.info("Mensaje recibido de SQS: {}", message.body());
            processMessage(message.body());
            deleteMessage(message.receiptHandle());
        }
    }

    private void processMessage(String body) {
        // Aquí procesarías el evento: actualizar estado, notificar, etc.
        log.info("Procesando evento de orden: {}", body);
    }

    private void deleteMessage(String receiptHandle) {
        sqsClient.deleteMessage(DeleteMessageRequest.builder()
                .queueUrl(ordersQueueUrl)
                .receiptHandle(receiptHandle)
                .build());
        log.info("Mensaje eliminado de la cola SQS");
    }
}