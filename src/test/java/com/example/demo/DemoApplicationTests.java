package com.example.demo;

import org.apache.camel.*;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class DemoApplicationTests {

	@EndpointInject(uri = "mock:response")
	private MockEndpoint mock;

	@EndpointInject(uri = "direct:start")
	private ProducerTemplate template;

	@EndpointInject(uri = "direct:start")
	private Endpoint context;

	private Exchange createRequest(){
		String MODEL_NAMESPACE = "https://kiegroup.org/dmn/_5388429C-0B33-4FA1-95DD-FA7A69AF31E6";
		String MODEL_NAME = "SampleDMN";
		String DMN_CONTEXT = "{\n    \"Input\": [\n        {\"DOM\": \"01/01/1970\", \"hasLicense?\": true, \"isLicenseValid?\": true},\n        {\"DOM\": \"01/01/1970\", \"hasLicense?\": false, \"isLicenseValid?\": true},\n        {\"DOM\": \"01/01/1970\", \"hasLicense?\": true, \"isLicenseValid?\": false},\n        {\"DOM\": \"01/01/1970\", \"hasLicense?\": false, \"isLicenseValid?\": false}\n      ]\n    }";

		Exchange exchange = context.createExchange();
		Message msg = exchange.getIn();

		msg.setHeader("model-namespace", MODEL_NAMESPACE);
		msg.setHeader("model-name", MODEL_NAME);
		msg.setHeader("dmn-context", DMN_CONTEXT);

		return exchange;
	}

	@Test
	public void contextLoads() throws InterruptedException {
		mock.expectedBodiesReceived("true,false,false,false");
		mock.expectedMessageCount(1);

		template.send("direct:start", createRequest());

		mock.assertIsSatisfied();
	}

}
