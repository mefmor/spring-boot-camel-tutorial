package net.mefmor.tutorial.spring.boot.camel.tutorial.error.handling;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.Test;

class FailureProcessorTest extends CamelTestSupport {

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            class FailureProcessor implements Processor {
                @Override
                public void process(Exchange exchange) {
                    Exception e = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
                    String failure = "The message failed because " + e.getMessage();
                    exchange.getIn().setHeader("FailureMessage", failure);
                }
            }

            @Override
            public void configure() {
                errorHandler(deadLetterChannel("mock:dead")
                        .useOriginalMessage().onPrepareFailure(new FailureProcessor()));

                from("direct:start")
                        .transform(constant("This is a changed body"))
                        .throwException(new IllegalArgumentException("Forced"));

            }
        };
    }

    @Test
    void testEnrichFailure() throws Exception {
        getMockEndpoint("mock:dead").expectedBodiesReceived("Hello World");
        getMockEndpoint("mock:dead").expectedHeaderReceived("FailureMessage", "The message failed because Forced");

        template.sendBody("direct:start", "Hello World");

        assertMockEndpointsSatisfied();
    }
}
