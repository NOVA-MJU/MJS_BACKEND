package nova.mjs.config.elasticsearch;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(basePackages = "nova.mjs.domain.thingo.ElasticSearch.Repository")
public class ElasticsearchConfig {}
