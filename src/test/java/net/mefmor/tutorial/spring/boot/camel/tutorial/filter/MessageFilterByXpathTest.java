package net.mefmor.tutorial.spring.boot.camel.tutorial.filter;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.Test;

public class MessageFilterByXpathTest extends CamelTestSupport {
    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            @Override
            public void configure() {
                from("direct:xml")
                        .filter(xpath("//Message[@flag='1']"))
                        .to("mock:result");
            }
        };
    }

    @Test
    void inputAndOutput() throws InterruptedException {
        getMockEndpoint("mock:result").expectedBodiesReceived("<Message id='1' flag='1' />", "<Message id='3' flag='1' />");

        template.sendBody("direct:xml", "<Message id='1' flag='1' />");
        template.sendBody("direct:xml", "<Message id='2' flag='3' />");
        template.sendBody("direct:xml", "<Message id='3' flag='1' />");
        template.sendBody("direct:xml", "<Message id='4' />");

        assertMockEndpointsSatisfied();
    }

}
