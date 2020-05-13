package net.mefmor.tutorial.spring.boot.camel.tutorial;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.Test;

public class ChoiceTest extends CamelTestSupport {
    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            @Override
            public void configure() {
                from("direct:start")
                        .choice()
                        .when(body().endsWith(".xml")).to("mock:xml")
                        .when(body().endsWith(".csv")).to("mock:csv")
                        .otherwise().to("mock:other");
            }
        };
    }

    @Test
    void testChoice() throws InterruptedException {
        getMockEndpoint("mock:xml").expectedMessageCount(1);
        getMockEndpoint("mock:csv").expectedMessageCount(1);
        getMockEndpoint("mock:other").expectedMessageCount(1);

        template.sendBody("direct:start", "extensible_markup_language_file.xml");
        template.sendBody("direct:start", "comma_separated_values_file.csv");
        template.sendBody("direct:start", ".gitignore");

        assertMockEndpointsSatisfied();
    }
}
