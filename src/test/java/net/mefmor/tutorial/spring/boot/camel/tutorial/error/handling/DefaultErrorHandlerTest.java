package net.mefmor.tutorial.spring.boot.camel.tutorial.error.handling;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class DefaultErrorHandlerTest extends CamelTestSupport {
    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            @Override
            public void configure() {
                from("direct:start")
                        .to("mock:first")
                        .to("mock:second");
            }
        };
    }

    @Test
    void testCaseOfExceptionOnTheFirstEndpoint() throws InterruptedException {
        getMockEndpoint("mock:first").expectedMessageCount(1);
        getMockEndpoint("mock:second").expectedMessageCount(0);

        getMockEndpoint("mock:first")
                .whenAnyExchangeReceived(e -> e.setException(new Exception("Some exception")));


        assertThrows(Exception.class, () -> template.sendBody("direct:start", "Any string"));


        assertMockEndpointsSatisfied();
    }
}
