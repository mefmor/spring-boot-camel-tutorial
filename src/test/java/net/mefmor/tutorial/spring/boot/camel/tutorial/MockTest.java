package net.mefmor.tutorial.spring.boot.camel.tutorial;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MockTest extends CamelTestSupport {

    private MockEndpoint mock;

    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            @Override
            public void configure() {
                from("stub:jms:topic:quote").to("mock:quote");
            }
        };
    }

    @BeforeEach
    void mockEndpointSetup() {
        mock = getMockEndpoint("mock:quote");
    }

    @Test
    void testExpectedMessageCount() throws InterruptedException {
        mock.expectedMessageCount(1);

        template.sendBody("stub:jms:topic:quote", "Camel rocks");

        mock.assertIsSatisfied();
    }

    @Test
    void testExpectedBodies() throws InterruptedException {
        mock.expectedBodiesReceived("Camel rocks", "Hello Camel");

        template.sendBody("stub:jms:topic:quote", "Camel rocks");
        template.sendBody("stub:jms:topic:quote", "Hello Camel");

        mock.assertIsSatisfied();
    }

    @Test
    void testIsCamelMessage() throws InterruptedException {
        mock.expectedMessageCount(2);
        mock.message(0).body().contains("Camel");
        mock.message(1).body().contains("Camel");

        template.sendBody("stub:jms:topic:quote", "Hello Camel");
        template.sendBody("stub:jms:topic:quote", "Camel rocks");

        assertMockEndpointsSatisfied();
    }

    @Test
    void testRegexMessageBody() throws InterruptedException {
        mock.allMessages().body().regex(".*Camel.*\\.");
        mock.allMessages().body().endsWith(".");

        template.sendBody("stub:jms:topic:quote", "Hello Camel.");
        template.sendBody("stub:jms:topic:quote", "Camel rocks.");

        assertMockEndpointsSatisfied();
    }
}
