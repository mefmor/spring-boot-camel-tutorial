package net.mefmor.tutorial.spring.boot.camel.tutorial;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.Test;

class BeanTransformingTest extends CamelTestSupport {
    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            @Override
            public void configure() {
                from("direct:xml").bean(new TransformBean()).to("mock:result");
            }
        };
    }

    private static class TransformBean {
        public String transform(String name) {
            return String.format("Hello, %s!", name);
        }
    }

    @Test
    void inputAndOutput() throws InterruptedException {
        getMockEndpoint("mock:result").expectedBodiesReceived("Hello, Boris!");

        template.sendBody("direct:xml", "Boris");

        assertMockEndpointsSatisfied();
    }
}
