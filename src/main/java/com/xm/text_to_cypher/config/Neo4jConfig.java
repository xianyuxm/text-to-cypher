package com.xm.text_to_cypher.config;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Config;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class Neo4jConfig {

    @Value("${neo4j.driver.uri}")
    private String uri;
    @Value("${neo4j.driver.user}")
    private String user;
    @Value("${neo4j.driver.password}")
    private String password;

    /**
     * Driver 为线程安全的单例，可重用连接池，建议应用启动时创建并在容器中管理
     */
    @Bean
    public Driver neo4jDriver() {
        return GraphDatabase.driver(
                uri,
                AuthTokens.basic(user, password),
                Config.builder()
                        .withMaxConnectionPoolSize(50)
                        .withConnectionAcquisitionTimeout(5, TimeUnit.SECONDS)
                        .build()
        );
    }

}
