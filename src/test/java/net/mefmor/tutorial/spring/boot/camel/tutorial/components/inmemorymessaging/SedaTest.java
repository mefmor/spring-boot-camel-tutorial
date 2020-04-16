package net.mefmor.tutorial.spring.boot.camel.tutorial.components.inmemorymessaging;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.Test;

public class SedaTest extends CamelTestSupport {

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            @Override
            public void configure() {
                from("direct:start").to("seda:incoming");

                from("seda:incoming").choice()
                        .when(body().endsWith(".xml")).to("seda:xml")
                        .when(body().endsWith(".csv")).to("seda:csv");

                from("seda:xml?multipleConsumers=true").to("mock:uat");
                from("seda:xml?multipleConsumers=true").to("mock:production");

            }
        };
    }

    @Test
    void getResponse() throws InterruptedException {
        getMockEndpoint("mock:uat").expectedMessageCount(1);
        getMockEndpoint("mock:production").expectedMessageCount(1);

        template.sendBody("direct:start", "input.xml");

        assertMockEndpointsSatisfied();
    }
}
