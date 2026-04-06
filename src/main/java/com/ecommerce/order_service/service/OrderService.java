package com.ecommerce.order_service.service;


import com.ecommerce.order_service.dto.CreateOrderRequest;
import com.ecommerce.order_service.model.Order;
import com.ecommerce.order_service.model.OrderItem;
import com.ecommerce.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final SqsClient sqsClient;
    private final CloudWatchLogService cloudWatchLogService;

    @Value("${aws.sqs.orders-queue-url}")
    private String ordersQueueUrl;

    @Value("${aws.sqs.lambda-orders-queue-url}")
    private String lambdaOrdersQueueUrl;

    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        log.info("Creando orden para cliente={}, items={}", request.customerId(), request.items().size());

        Order order = new Order();
        order.setCustomerId(request.customerId());

        request.items().forEach(itemRequest -> {
            OrderItem item = new OrderItem();
            item.setProductId(itemRequest.productId());
            item.setProductName(itemRequest.productName());
            item.setQuantity(itemRequest.quantity());
            item.setPrice(itemRequest.price());
            order.addItem(item);
        });

        BigDecimal total = order.getItems().stream()
                .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(total);

        Order saved = orderRepository.save(order);
        log.info("Orden guardada: id={}, cliente={}, total={}", saved.getId(), saved.getCustomerId(), saved.getTotalAmount());
        cloudWatchLogService.sendLog(String.format("ORDER_CREATED orderId=%s customerId=%s total=%s", saved.getId(), saved.getCustomerId(), saved.getTotalAmount()));

        publishOrderCreatedEvent(saved);
        return saved;
    }

    public Order getOrder(UUID id) {
        log.info("Consultando orden id={}", id);
        return orderRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Orden no encontrada: id={}", id);
                    return new RuntimeException("Orden no encontrada: " + id);
                });
    }

    public List<Order> getOrdersByCustomer(String customerId) {
        log.info("Consultando órdenes para cliente={}", customerId);
        List<Order> orders = orderRepository.findByCustomerId(customerId);
        log.info("Se encontraron {} órdenes para cliente={}", orders.size(), customerId);
        return orders;
    }

    private void publishOrderCreatedEvent(Order order) {
        String message = String.format(
                "{\"orderId\":\"%s\",\"customerId\":\"%s\",\"totalAmount\":%s,\"status\":\"%s\"}",
                order.getId(), order.getCustomerId(), order.getTotalAmount(), order.getStatus()
        );

        try {
            sqsClient.sendMessage(SendMessageRequest.builder()
                    .queueUrl(ordersQueueUrl)
                    .messageBody(message)
                    .build());
            log.info("Evento publicado en SQS para orden={}", order.getId());

            sqsClient.sendMessage(SendMessageRequest.builder()
                    .queueUrl(lambdaOrdersQueueUrl)
                    .messageBody(message)
                    .build());
            log.info("Evento publicado en lambda-orders-queue para orden={}", order.getId());
        } catch (Exception e) {
            log.error("Error al publicar evento SQS para orden={}: {}", order.getId(), e.getMessage());
        }
    }
}


