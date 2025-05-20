package nova.mjs.util.ElasticSearch.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(basePackages = "nova.mjs.util.ElasticSearch.Repository")
public class ElasticsearchConfig {}
