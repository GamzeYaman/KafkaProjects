package org.app;

import com.google.gson.JsonParser;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.opensearch.action.bulk.BulkRequest;
import org.opensearch.action.bulk.BulkResponse;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.action.index.IndexResponse;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.client.indices.CreateIndexRequest;
import org.opensearch.client.indices.GetIndexRequest;
import org.opensearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class OpenSearchConsumer {

        public static RestHighLevelClient createOpenSearchClient() {
            String connString = "http://localhost:9200";
//        String connString = "https://c9p5mwld41:45zeygn9hy@kafka-course-2322630105.eu-west-1.bonsaisearch.net:443";

            // we build a URI from the connection string
            RestHighLevelClient restHighLevelClient;
            URI connUri = URI.create(connString);
            // extract login information if it exists
            String userInfo = connUri.getUserInfo();

            if (userInfo == null) {
                // REST client without security
                restHighLevelClient = new RestHighLevelClient(RestClient.builder(new HttpHost(connUri.getHost(), connUri.getPort(), "http")));

            } else {
                // REST client with security
                String[] auth = userInfo.split(":");

                CredentialsProvider cp = new BasicCredentialsProvider();
                cp.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(auth[0], auth[1]));

                restHighLevelClient = new RestHighLevelClient(
                        RestClient.builder(new HttpHost(connUri.getHost(), connUri.getPort(), connUri.getScheme()))
                                .setHttpClientConfigCallback(
                                        httpAsyncClientBuilder -> httpAsyncClientBuilder.setDefaultCredentialsProvider(cp)
                                                .setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy())));


            }

            return restHighLevelClient;
        }

       public static KafkaConsumer<String, String> createKafkaConsumer() {
            String boostrapServers = "127.0.0.1:9092";
            String groupId = "consumer-opensearch-demo";

           Properties properties = new Properties();
           properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, boostrapServers);
           properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
           properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
           properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
           properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

           return new KafkaConsumer<>(properties);

       }

       private static String extractId(String json){
            return JsonParser.parseString(json)
                    .getAsJsonObject()
                    .get("meta")
                    .getAsJsonObject()
                    .get("id")
                    .getAsString();
       }

    public static void main(String[] args) throws IOException {

        Logger log = LoggerFactory.getLogger(OpenSearchConsumer.class.getSimpleName());

        //Opensearch client
        RestHighLevelClient openSearchClient  = createOpenSearchClient();

        KafkaConsumer<String, String> consumer = createKafkaConsumer();

        try(openSearchClient; consumer){

            boolean indexExists = openSearchClient.indices().exists(new GetIndexRequest("wikimedia"), RequestOptions.DEFAULT);

            if(!indexExists){
                CreateIndexRequest createIndexRequest = new CreateIndexRequest("wikimedia");
                openSearchClient.indices().create(createIndexRequest, RequestOptions.DEFAULT);
                log.info("The Wikimedia Index has been created!");
            }else {
                log.info("The Wikimedia Index already exists.");
            }

            consumer.subscribe(Collections.singleton("wikimedia.recentchange"));

            while(true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(3000));

                int recordCount = records.count();
                log.info("Received " + recordCount + " records");

                BulkRequest bulkRequest = new BulkRequest();

                for(ConsumerRecord<String, String> record : records) {

                    //Idempotence Strategy 1 : Define an ID using Kafka Record coordinates
                    //String id = record.topic() + "_" + record.partition() + "_" + record.offset();

                    //Strategy2
                    //extract the id from the JSON
                    String id = extractId(record.value());

                    IndexRequest indexRequest = new IndexRequest("wikimedia")
                            .source(record.value(), XContentType.JSON)
                            .id(id);

                  //  IndexResponse response = openSearchClient.index(indexRequest, RequestOptions.DEFAULT);

                    bulkRequest.add(indexRequest);

                }
                if(bulkRequest.numberOfActions() > 0){
                    BulkResponse response = openSearchClient.bulk(bulkRequest, RequestOptions.DEFAULT);
                    log.info("Inserted " + response.getItems().length + " record(s)");

                    try{
                        Thread.sleep(1000);
                    }catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

        } catch (Exception e) {

        }



        //Kafka Client

        //main code logic

        //close things
    }
}
