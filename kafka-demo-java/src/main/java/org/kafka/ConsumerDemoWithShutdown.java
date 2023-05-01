package org.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

public class ConsumerDemoWithShutdown {
    private static final Logger log = LoggerFactory.getLogger(ConsumerDemoWithShutdown.class.getSimpleName());
    private static final String topicName = "SECOND_TOPIC";

    public static void main(String[] args) {
        log.info("I am Kafka Consumer :)");

        String groupId = "my-java-application";

        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "127.0.0.1:9092");
        properties.setProperty("key.deserializer", StringDeserializer.class.getName());
        properties.setProperty("value.deserializer", StringDeserializer.class.getName());
        properties.setProperty("group.id", groupId);
        properties.setProperty("auto.offset.reset", "earliest");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);

        // get a reference to the main thread
        final Thread mainThread = Thread.currentThread();

        // adding the shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                log.info("Detected a shutdown, let's exit by calling consumer.wakeup()....");
                consumer.wakeup();

                // join the main thread to allow the execution of the code in the main thread
                try {
                    mainThread.join();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        try {
            consumer.subscribe(Arrays.asList(topicName));

            while (true) {
                log.info("Messages are being pulled");

                ConsumerRecords<String, String> recordList = consumer.poll(Duration.ofMillis(2000));
                log.info("Messages were pulled");

                String polledMessage;
                for(ConsumerRecord<String, String> record: recordList){
                    polledMessage = record.value().toUpperCase();
                    log.info("Key: " + record.key() + " | Partition: " + record.partition() + " | Offset: " + record.offset() + "\nMessage: " + polledMessage);
                }
            }
         }catch (WakeupException e) {
            log.info("Consumer is starting to shutdown");
         }catch (Exception e){
            log.error("Unexcepted exception in consumer", e);
         }finally {
            consumer.close(); // close the consumer, this will also commit offsets
            log.info("Consumer was closed gracefully!");
         }

    }
}
