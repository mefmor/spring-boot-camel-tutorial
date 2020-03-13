package net.mefmor.tutorial.spring.boot.camel.tutorial;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.Test;


class TokenizeXmlSplitTest extends CamelTestSupport {
    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            @Override
            public void configure() {
                from("direct:xml")
                        .split().tokenizeXML("Info")
                        .to("mock:result");
            }
        };
    }

    @Test
    void inputAndOutput() throws InterruptedException {
        getMockEndpoint("mock:result").expectedBodiesReceived("<Info id='1'/>", "<Info id='2'/>");

        template.sendBody("direct:xml", "<XML><Batch><Info id='1'/><Info id='2'/></Batch></XML>");

        assertMockEndpointsSatisfied();
    }

}
