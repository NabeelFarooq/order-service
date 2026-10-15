
package com.ecommerce.order.service;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ecommerce.order.entity.OutboxEvent;
import com.ecommerce.order.repository.OutboxEventRepository;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.propagation.Propagator;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Component
public class OutboxPublisher {

    private static final Logger log =
            LoggerFactory.getLogger(OutboxPublisher.class);

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final Tracer tracer;
    private final Propagator propagator;

    public OutboxPublisher(
            OutboxEventRepository outboxEventRepository,
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            Tracer tracer,
            Propagator propagator) {

        this.outboxEventRepository = outboxEventRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.tracer = tracer;
        this.propagator = propagator;
    }

    @Scheduled(fixedDelay = 5000)
    public void publishEvents() {

        List<OutboxEvent> events =
                outboxEventRepository
                        .findTop100ByStatusOrderByCreatedAtAsc("NEW");

        for (OutboxEvent event : events) {

            Span publishSpan = null;

            try {
                publishSpan = createPublishingSpan(event);

                try (Tracer.SpanInScope ignored =
                             tracer.withSpan(publishSpan)) {

                    kafkaTemplate.send(
                            event.getTopic(),
                            event.getMessageKey() != null
                                    ? event.getMessageKey()
                                    : event.getEventId(),
                            event.getPayload()
                    ).get();
                    
                    event.setStatus("PUBLISHED");
                    outboxEventRepository.save(event);

                    log.info(
                            "Outbox event published. eventId={}, topic={}",
                            event.getEventId(),
                            event.getTopic()
                    );
                }

                

            } catch (Exception e) {

                if (publishSpan != null) {
                    publishSpan.error(e);
                }

                log.error(
                        "Failed to publish outbox event. eventId={}",
                        event.getEventId(),
                        e
                );

            } finally {
                if (publishSpan != null) {
                    publishSpan.end();
                }
            }
        }
    }

    private Span createPublishingSpan(OutboxEvent event)
            throws Exception {

        String savedContext = event.getTraceContext();

        if (savedContext != null && !savedContext.isBlank()) {

            Map<String, String> carrier =
                    objectMapper.readValue(
                            savedContext,
                            new TypeReference<Map<String, String>>() {}
                    );

            if (!carrier.isEmpty()) {
                return propagator
                        .extract(carrier, (map, key) -> map.get(key))
                        .name("order-outbox-publish")
                        .start();
            }
        }

        // Fallback for older outbox rows with no saved trace context.
        return tracer.nextSpan()
                .name("order-outbox-publish")
                .start();
    }
}
