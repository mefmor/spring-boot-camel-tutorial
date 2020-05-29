package net.mefmor.tutorial.spring.boot.camel.tutorial.split;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class SplitListTest extends CamelTestSupport {
    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            @Override
            public void configure() {
                from("direct:list")
                        .split().body()
                        .to("mock:result");
            }
        };
    }

    @Test
    void inputAndOutput() throws InterruptedException {
        getMockEndpoint("mock:result").expectedBodiesReceived("One", "Two", "Three");

        template.sendBody("direct:list", Arrays.asList("One", "Two", "Three"));

        assertMockEndpointsSatisfied();
    }

}