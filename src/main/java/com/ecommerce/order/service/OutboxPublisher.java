package com.ecommerce.order.service;

import java.util.List;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ecommerce.order.entity.OutboxEvent;
import com.ecommerce.order.repository.OutboxEventRepository;

@Component
public class OutboxPublisher {

	private final OutboxEventRepository outboxEventRepository;
	private final KafkaTemplate<String, String> kafkaTemplate;

	public OutboxPublisher(OutboxEventRepository outboxEventRepository, KafkaTemplate<String, String> kafkaTemplate) {

		this.outboxEventRepository = outboxEventRepository;
		this.kafkaTemplate = kafkaTemplate;
	}

	@Scheduled(fixedDelay = 5000)
	public void publishEvents() {

		List<OutboxEvent> events = outboxEventRepository.findTop100ByStatusOrderByCreatedAtAsc("NEW");

		for (OutboxEvent event : events) {

			try {

				kafkaTemplate.send(event.getTopic(),
						event.getMessageKey() != null ? event.getMessageKey() : event.getEventId(), event.getPayload())
						.get();

				event.setStatus("PUBLISHED");

				outboxEventRepository.save(event);

			} catch (Exception e) {

				System.err.println("Failed to publish event: " + event.getEventId());

			}
		}
	}
}