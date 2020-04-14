package net.mefmor.tutorial.spring.boot.camel.tutorial;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.Test;

public class MockTest extends CamelTestSupport {

    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            @Override
            public void configure() {
                from("stub:jms:topic:quote").to("mock:quote");
            }
        };
    }

    @Test
    void testQuote() throws InterruptedException {
        MockEndpoint quote = getMockEndpoint("mock:quote");
        quote.expectedMessageCount(1);

        template.sendBody("stub:jms:topic:quote", "Camel rocks");

        quote.assertIsSatisfied();
    }
}
