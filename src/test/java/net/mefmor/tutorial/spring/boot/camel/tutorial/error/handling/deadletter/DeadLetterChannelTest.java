package net.mefmor.tutorial.spring.boot.camel.tutorial.error.handling.deadletter;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.Test;

public class DeadLetterChannelTest extends CamelTestSupport {
    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            @Override
            public void configure() {
                errorHandler(deadLetterChannel("mock:dead").useOriginalMessage());

                from("direct:start")
                        .to("mock:first")
                        .to("mock:second");
            }
        };
    }

    @Test
    void byDefaultAllEndpointsExceptDeadReceiveMessages() throws InterruptedException {
        getMockEndpoint("mock:first").expectedMessageCount(1);
        getMockEndpoint("mock:second").expectedMessageCount(1);
        getMockEndpoint("mock:dead").expectedMessageCount(0);

        template.sendBody("direct:start", "Message");

        assertMockEndpointsSatisfied();
    }

    @Test
    void testInCaseOnExceptionOnFirstEndpoint() throws InterruptedException {
        getMockEndpoint("mock:first").expectedMessageCount(1);
        getMockEndpoint("mock:second").expectedMessageCount(0);
        getMockEndpoint("mock:dead").expectedMessageCount(1);

        getMockEndpoint("mock:first")
                .whenAnyExchangeReceived(e -> e.setException(new Exception("Some exception")));

        template.sendBody("direct:start", "Message");

        assertMockEndpointsSatisfied();
    }

    @Test
    void testInCaseOnExceptionOnSecondEndpoint() throws InterruptedException {
        getMockEndpoint("mock:first").expectedMessageCount(1);
        getMockEndpoint("mock:second").expectedMessageCount(1);
        getMockEndpoint("mock:dead").expectedMessageCount(1);

        getMockEndpoint("mock:second")
                .whenAnyExchangeReceived(e -> e.setException(new Exception("Some exception")));

        template.sendBody("direct:start", "Message");

        assertMockEndpointsSatisfied();
    }
}
