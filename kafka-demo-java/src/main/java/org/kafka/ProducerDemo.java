package org.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class ProducerDemo {
    private static final Logger log = LoggerFactory.getLogger(ProducerDemo.class.getSimpleName());

    public static void main(String[] args) {
        log.info("I am Kafka Producer :)");

        //Producer Properties
        Properties properties = new Properties();
        //connect localhost
        properties.setProperty("bootstrap.servers", "127.0.0.1:9092");

        //connect kafdrop
        /*properties.setProperty("security.protocol", "SASL_SSL");
        properties.setProperty("sasl.jaas.config", "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"foo\" password=\"bar\";");
        properties.setProperty("sasl.mechanism", "SCRAM-SHA-512");*/

        //set producer properties
        properties.setProperty("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.setProperty("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        //create producer
        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);

        //create Producer Record
        //ProducerRecord<String, String> producerRecord = new ProducerRecord<>("DEMO_KAFKA", "How Are You Today?:)");
        ProducerRecord<String, String> producerRecord = new ProducerRecord<>("SECOND_TOPIC", "YOLO", "Keep It Simple Stupid");

        //send data
        producer.send(producerRecord);

        //flush and close producer
        producer.flush();
        producer.close();


    }
}
