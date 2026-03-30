package it.aruba.delivery.request.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import it.aruba.delivery.request.dto.RequestStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeliveryEventPublisher {

	@Value("${kafka.topic.request-status}")
	private String topic;

	private final KafkaTemplate<String, RequestStatusChangedEvent> kafkaTemplate;

	public void publishStatusChanged(RequestStatusChangedEvent event) {
        //quando esaurisce i tentativi impostati da config logga l'errore
	    kafkaTemplate.send(topic, String.valueOf(event.getRequestId()), event)
	        .whenComplete((result, ex) -> {
	            if (ex != null) {
	                log.error("Kafka publish FAILED for request {}", event.getRequestId(), ex);
	            } else {
	            	log.info("Kafka publish SUCCESS for request {} - status {} -> {}",
	            		    event.getRequestId(),
	            		    event.getOldStatus(),
	            		    event.getNewStatus()
	            		);
	            }
	        });
	}
}