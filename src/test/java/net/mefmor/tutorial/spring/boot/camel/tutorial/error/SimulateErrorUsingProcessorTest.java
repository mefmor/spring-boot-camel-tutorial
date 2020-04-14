package net.mefmor.tutorial.spring.boot.camel.tutorial.error;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.ConnectException;

public class SimulateErrorUsingProcessorTest extends CamelTestSupport {

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            @Override
            public void configure() {
                context.setTracing(true);

                errorHandler(defaultErrorHandler().maximumRedeliveries(5).redeliveryDelay(1000));

                onException(IOException.class).maximumRedeliveries(3).handled(true).to("mock:ftp");

                from("direct:file")
                        .process(e -> {
                            throw new ConnectException("Simulated connection error");
                        })
                        .to("mock:http");
            }
        };
    }

    @Test
    void testSimulateErrorUsingProcessor() throws Exception {
        getMockEndpoint("mock:http").expectedMessageCount(0);

        MockEndpoint ftp = getMockEndpoint("mock:ftp");
        ftp.expectedBodiesReceived("Camel rocks");

        template.sendBody("direct:file", "Camel rocks");

        assertMockEndpointsSatisfied();
    }

}
