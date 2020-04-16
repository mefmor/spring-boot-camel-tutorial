package net.mefmor.tutorial.spring.boot.camel.tutorial.error.handling;

import org.apache.camel.Headers;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.Test;

import java.util.Map;

class FailureBeanTest extends CamelTestSupport {

    public static class FailureBean {
        public void enrich(@Headers Map<String, String> headers, Exception cause) {
            String failure = "The message failed because " + cause.getMessage();
            headers.put("FailureMessage", failure);
        }
    }

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            @Override
            public void configure() {
                errorHandler(deadLetterChannel("direct:dead").useOriginalMessage());

                from("direct:start")
                        .transform(constant("This is a changed body"))
                        .throwException(new IllegalArgumentException("Forced"));

                from("direct:dead")
                        .bean(new FailureBean())
                        .to("mock:dead");

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
