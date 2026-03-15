package org.example.webmetathreads;

import org.example.webmetathreads.config.ThreadsApiProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
public class WebMetaThreadsApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebMetaThreadsApplication.class, args);
    }

}
