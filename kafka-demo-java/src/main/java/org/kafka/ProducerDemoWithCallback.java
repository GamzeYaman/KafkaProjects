package org.kafka;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class ProducerDemoWithCallback {
    private static final Logger log = LoggerFactory.getLogger(ProducerDemoWithCallback.class.getSimpleName());
    private static String topicName = "THIRD_TOPIC";

    public static void main(String[] args) {
        log.info("I am Kafka Producer :)");

        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "127.0.0.1:9092");
        properties.setProperty("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.setProperty("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        KafkaProducer<String, String> producer = new KafkaProducer<String, String>(properties);

        String members[] = {"Kim Namjoon", "Kim Seokjin", "Min Yoongi", "Jung Hoseok", "Park Jimin", "Kim Taehyung", "Jeon Jungkook"};
        String message;

        for(int j = 0; j < 3; j++){
            for (int i = 0; i < members.length; i++){
                message = members[i];
                sendMessageToKafka(producer, message);
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        producer.flush();
        producer.close();
    }

    private static void sendMessageToKafka(KafkaProducer<String, String> producer, String message) {
        ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topicName, message);

        producer.send(producerRecord, new Callback() {
            @Override
            public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                // executes every time a record successfully sent or an exception is thrown
                if(e != null){
                    log.error("Error while producing! :( \n" +
                            "Problem : " + e.getMessage());
                }
                log.info("Received new metadata! ;)\n" + "Topic: " + recordMetadata.topic() + "\t"
                + "Partition: " + recordMetadata.partition() + "\t"
                + "Offset: " + recordMetadata.offset() + "\t"
                + "Timestamp: " + recordMetadata.timestamp());
            }
        });
    }
}
