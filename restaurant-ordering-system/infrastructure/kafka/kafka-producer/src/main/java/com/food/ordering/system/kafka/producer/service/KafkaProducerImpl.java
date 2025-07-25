package com.food.ordering.system.kafka.producer.service;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

@Slf4j
@Component
public class KafkaProducerImpl<K extends Serializable, V extends SpecificRecordBase> implements KafkaProducer<K, V> {

    private final KafkaTemplate<K, V> kafkaTemplate;

    public KafkaProducerImpl(KafkaTemplate<K, V> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void send(String topicName, K key, V message, BiConsumer<SendResult<K, V>, Throwable> callback) {
        log.info("Sending message = {} to topic = {}", message, topicName);
        // now returns a java.util.concurrent.CompletableFuture

        CompletableFuture<SendResult<K, V>> kafkaResultFuture = kafkaTemplate.send(topicName, key, message);

        // attach success/failure handlers
        kafkaResultFuture.whenComplete((result, ex) -> {
            if (ex != null) {
                callback.accept(null, ex);
            } else {
                callback.accept(result, null);
            }
        });

    }

    @PreDestroy
    public void close() {
        if (kafkaTemplate != null) {
            log.info("Closing Kafka producer!");
            kafkaTemplate.destroy();
        }
    }
}
