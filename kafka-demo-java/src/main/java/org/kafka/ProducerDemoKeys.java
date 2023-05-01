package org.kafka;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class ProducerDemoKeys {
    private static final Logger log = LoggerFactory.getLogger(ProducerDemoKeys.class.getSimpleName());
    private static String topicName = "SECOND_TOPIC";

    public static void main(String[] args) {
        log.info("I am Kafka Producer");

        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "127.0.0.1:9092");
        properties.setProperty("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.setProperty("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        KafkaProducer<String, String> producer = new KafkaProducer<String, String>(properties);

        String members[] = {"Kim Jisoo", "Kim Jennie", "Park Chaeyoung", "Lalisa"};
        String message, key;
        for(int j = 0; j < 3; j++){
            for(int i = 0; i < members.length; i++){
                key = "id" + i;
                message = members [i];
                sendMessageToKafka(producer, key, message);
            }
            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

    }

    private static void sendMessageToKafka(KafkaProducer<String, String> producer, String key, String message) {
        ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topicName, key, message);
        producer.send(producerRecord, new Callback() {
            @Override
            public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                if(e != null) {
                    log.error("Error while producing! :( \n" +
                            "Problem : " + e.getMessage());
                } else{
                    log.info("Key: " + key + " | Partition: " + recordMetadata.partition() + "\t" +
                            "Offset: " + recordMetadata.offset() + "\t" +
                            "Timestamp: " + recordMetadata.timestamp());
                }
            }
        });
    }
}
