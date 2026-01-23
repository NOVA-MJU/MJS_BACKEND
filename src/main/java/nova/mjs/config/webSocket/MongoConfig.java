package nova.mjs.config.webSocket;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
@RequiredArgsConstructor
public class MongoConfig {
    private final MongoProperties mongoProperties;

    @Bean
    public MongoClient mongoClient() {
        return MongoClients.create(mongoProperties.getUri());
    }

    @Bean
    public MongoTemplate mongoTemplate() {
        String db = mongoProperties.getDatabase();
        if (db == null || db.isBlank()) {
            db = new ConnectionString(mongoProperties.getUri()).getDatabase();
        }
        if (db == null || db.isBlank()) {
            throw new IllegalArgumentException("MongoDB database name is missing. Set spring.data.mongodb.database or include it in uri.");
        }
        return new MongoTemplate(mongoClient(), db);
    }
}
