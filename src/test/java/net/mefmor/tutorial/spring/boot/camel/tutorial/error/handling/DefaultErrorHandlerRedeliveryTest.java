package net.mefmor.tutorial.spring.boot.camel.tutorial.error.handling;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class DefaultErrorHandlerRedeliveryTest extends CamelTestSupport {
    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            @Override
            public void configure() {
                errorHandler(defaultErrorHandler()
                        .maximumRedeliveries(2)
                        .redeliveryDelay(100)
                        .retryAttemptedLogLevel(LoggingLevel.WARN));

                from("direct:start")
                        .to("mock:first")
                        .to("mock:second");
            }
        };
    }

    @Test
    void errorHandlerTryToSendMessageTwoTimes() throws InterruptedException {
        getMockEndpoint("mock:first").expectedMessageCount(2);
        getMockEndpoint("mock:second").expectedMessageCount(1);

        getMockEndpoint("mock:first").whenExchangeReceived(1, e -> e.setException(new Exception("Some exception")));

        template.sendBody("direct:start", "Message");

        assertMockEndpointsSatisfied();
    }

    @Test
    void redeliveryHappensAtThePointOfError() throws InterruptedException {
        getMockEndpoint("mock:first").expectedMessageCount(1);
        getMockEndpoint("mock:second").expectedMessageCount(2);

        getMockEndpoint("mock:second").whenExchangeReceived(1, e -> e.setException(new Exception("Some exception")));

        template.sendBody("direct:start", "Message");

        assertMockEndpointsSatisfied();
    }

    @Test
    void afterAllAttemptsYouGetAnException() throws InterruptedException {
        getMockEndpoint("mock:first").expectedMessageCount(1);
        getMockEndpoint("mock:second").expectedMessageCount(3);

        getMockEndpoint("mock:second").whenAnyExchangeReceived(e -> e.setException(new Exception("Some exception")));

        assertThrows(Exception.class, () -> template.sendBody("direct:start", "Message"));

        assertMockEndpointsSatisfied();
    }
}
