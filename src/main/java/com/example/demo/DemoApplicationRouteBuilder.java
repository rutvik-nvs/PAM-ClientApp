package com.example.demo;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class DemoApplicationRouteBuilder extends RouteBuilder {

    public static final String TARGET_WITH_AUTH = "http:localhost:8080" +
            "/kie-server/services/rest/server/containers/Sample-DMN_1.0.0-SNAPSHOT/dmn" +
            "?authMethod=Basic&authUsername=kieserver&authPassword=kieserver1!&bridgeEndpoint=true";

    public static final String MODEL_NAMESPACE = "https://kiegroup.org/dmn/_5388429C-0B33-4FA1-95DD-FA7A69AF31E6";
    public static final String MODEL_NAME = "SampleDMN";
    public static final String DMN_CONTEXT = "{\n    \"Input\": [\n        {\"DOM\": \"01/01/1970\", \"hasLicense?\": true, \"isLicenseValid?\": true},\n        {\"DOM\": \"01/01/1970\", \"hasLicense?\": false, \"isLicenseValid?\": true},\n        {\"DOM\": \"01/01/1970\", \"hasLicense?\": true, \"isLicenseValid?\": false},\n        {\"DOM\": \"01/01/1970\", \"hasLicense?\": false, \"isLicenseValid?\": false}\n      ]\n    }";

    public static final String TARGET_WITH_AUTH_PAM = "http://localhost:8080" +
            "/kie-server/services/rest/server/containers/itorders_1.0.0-SNAPSHOT/processes/itorders-data.place-order/instances" +
            "?authMethod=Basic&authUsername=kieserver&authPassword=kieserver1!&bridgeEndpoint=true";

    public static final String SAMPLE_PROCESS_BODY = "{\"age\": 25,\"person\": {\"Person\": {\"name\": \"john\"}}}";

    DataSource datasource = setupDataSource();

    private static DataSource setupDataSource(){
        BasicDataSource ds = new BasicDataSource();
        ds.setUsername("postgres");
        ds.setDriverClassName("org.postgresql.Driver");
        ds.setPassword("password");
        ds.setUrl("jdbc:postgresql://localhost:5432/Data");
        return ds;
    }

    @Override
    public void configure() throws Exception {

        restConfiguration().component("servlet");

        rest("/api")
                .get("/records")
                .to("direct:start");

        // Kafka Route
        from("kafka:kafka-sample-topic?brokers=localhost:9092")
                .log("\n${body}")
                .to("mock:kafkaResponse");

        getContext().getRegistry().bind("datasource", datasource);

        // PostgreSQL Read => Create JSON Request => Invoke API => Get Response => Write
        from("direct:postgreSQL")
                    .transform(simple("SELECT * from datatable;"))
                .to("jdbc:datasource")
                .to("freemarker:templates/requestObject.ftl")
                    .setHeader("modelNamespace", constant(MODEL_NAMESPACE))
                    .setHeader("modelName", constant(MODEL_NAME))
                .to("freemarker:templates/template.ftl")
                    .log("\n${body}")
                    .setHeader("Content-Type", constant("application/json"))
                    .setHeader("Accept", constant("application/json"))
                    .setHeader(Exchange.HTTP_METHOD, constant("POST"))
                    .removeHeader(Exchange.HTTP_PATH)
                .to(TARGET_WITH_AUTH)
                    .log("\n${body}")
                    .setBody(simple("INSERT INTO dataresults (output, date) VALUES (true, '01/01/1970')"))
                .to("jdbc:datasource")
                    .log("${body}")
                .to("mock:postgreSQLResponse");


        // KIE-Server REST Api Invoker
        from("direct:start").id("start")
                    .setHeader("modelNamespace", constant(MODEL_NAMESPACE))
                    .setHeader("modelName", constant(MODEL_NAME))
                    .setBody(simple(DMN_CONTEXT))
                .to("freemarker:templates/template.ftl")
                    .setHeader("Content-Type", constant("application/json"))
                    .setHeader("Accept", constant("application/json"))
                    .setHeader(Exchange.HTTP_METHOD, constant("POST"))
                    .removeHeader(Exchange.HTTP_PATH)
                .log("\n${body}")
                .to(TARGET_WITH_AUTH)
                .split().jsonpath("result.dmn-evaluation-result.decision-results[*]")
                .choice()
                    .when().jsonpath("$.[?(@.decision-name == 'Decision')]")
                        .split().jsonpath(".result")
                        .log("${body}")
                .to("mock:response");

        // PAM KIE-Server REST Api Invoker for creating process instance
        from("direct:startTwo").id("startTwo")
                    .setHeader("Content-Type", constant("application/json"))
                    .setHeader("Accept", constant("application/json"))
                    .setHeader(Exchange.HTTP_METHOD, constant("POST"))
                    .removeHeader(Exchange.HTTP_PATH)
                .setBody(simple(SAMPLE_PROCESS_BODY))
            .to(TARGET_WITH_AUTH_PAM)
            .log("${body}")
            .to("mock:response");


    }
}