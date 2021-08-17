package com.example.demo;

import org.apache.camel.*;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class PostgreSQLTest {

    @EndpointInject(uri = "mock:postgreSQLResponse")
    private MockEndpoint mock;

    @EndpointInject(uri = "direct:postgreSQL")
    private ProducerTemplate template;

    @EndpointInject(uri = "direct:postgreSQL")
    private Endpoint context;

    private Exchange createRequest(){
        Exchange exchange = context.createExchange();
        return exchange;
    }

    @Test
    public void contextLoads() throws InterruptedException {
        mock.expectedMessageCount(1);

        template.send("direct:postgreSQL", createRequest());

        mock.assertIsSatisfied();
    }
}