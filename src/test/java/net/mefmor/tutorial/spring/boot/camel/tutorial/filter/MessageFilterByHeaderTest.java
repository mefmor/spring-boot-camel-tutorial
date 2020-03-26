package net.mefmor.tutorial.spring.boot.camel.tutorial.filter;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.Test;

public class MessageFilterByHeaderTest extends CamelTestSupport {
    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            @Override
            public void configure() {
                from("direct:string")
                        .filter(header("headerName").isEqualTo("true"))
                        .to("mock:result");
            }
        };
    }

    @Test
    void inputAndOutput() throws InterruptedException {
        getMockEndpoint("mock:result").expectedBodiesReceived("Message 2", "Message 4");

        template.sendBody("direct:string", "Message 1");
        template.sendBodyAndHeader("direct:string", "Message 2", "headerName", "true");
        template.sendBodyAndHeader("direct:string", "Message 3", "headerName", "false");
        template.sendBodyAndHeader("direct:string", "Message 4", "headerName", "true");

        assertMockEndpointsSatisfied();
    }
}
