package org.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.CooperativeStickyAssignor;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

public class ConsumerDemoCooperativeRebalance {
    private static final Logger log = LoggerFactory.getLogger(ConsumerDemoCooperativeRebalance.class.getSimpleName());
    private static String topicName = "SECOND_TOPIC";

    public static void main(String[] args) {
        log.info("I am Kafka Consumer :)");
        String groupId = "my-java-application";

        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "127.0.0.1:9092");
        properties.setProperty("key.deserializer", StringDeserializer.class.getName());
        properties.setProperty("value.deserializer", StringDeserializer.class.getName());
        properties.setProperty("group.id", groupId);
        properties.setProperty("auto.offset.reset", "earliest");
        properties.setProperty("partition.assignment.strategy", CooperativeStickyAssignor.class.getName());
        // properties.setProperty("group.instance.id", "...."); //Cooperative Rebalance

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);
        consumer.subscribe(Arrays.asList(topicName));

        while(true){
            ConsumerRecords<String, String> recordList = consumer.poll(Duration.ofMillis(2000));
            String polledMessage;
            for(ConsumerRecord<String, String> record: recordList){
                polledMessage = record.value().toUpperCase();
                log.info("Key: " + record.key() + " | Partition: " + record.partition() + " | Offset: " + record.offset() + "\nMessage: " + polledMessage);
            }
        }
    }
}
