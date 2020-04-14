package net.mefmor.tutorial.spring.boot.camel.tutorial.error;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.ConnectException;

public class SimulateErrorUsingMockTest extends CamelTestSupport {

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            @Override
            public void configure() {
                context.setTracing(true);

                errorHandler(defaultErrorHandler()
                        .maximumRedeliveries(5).redeliveryDelay(1000));

                onException(IOException.class).maximumRedeliveries(3)
                        .handled(true)
                        .to("mock:ftp");

                from("direct:file")
                        .to("mock:http");
            }
        };
    }

    @Test
    void testSimulateErrorUsingMock() throws Exception {
        getMockEndpoint("mock:ftp").expectedBodiesReceived("Camel rocks");

        MockEndpoint http = getMockEndpoint("mock:http");
        http.whenAnyExchangeReceived(e -> e.setException(new ConnectException("Simulated connection error")));

        template.sendBody("direct:file", "Camel rocks");

        assertMockEndpointsSatisfied();
    }

}