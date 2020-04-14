package net.mefmor.tutorial.spring.boot.camel.tutorial.advicewith;

import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.Test;

public class AdviceWithMockEndpointsTest extends CamelTestSupport {

    @Override
    public boolean isUseAdviceWith() {
        // remember to override this method and return true to tell Camel that we are using advice-with in the routes
        return true;
    }

    @Override
    protected RoutesBuilder createRouteBuilder() {
        return new RouteBuilder() {
            @Override
            public void configure() {
                from("seda:quotes").routeId("quotes")
                        .choice()
                        .when(simple("${body} contains 'Camel'"))
                        .to("seda:camel")
                        .otherwise()
                        .to("seda:other");
            }
        };
    }

    @Test
    void testMockEndpoints() throws Exception {
        AdviceWithRouteBuilder.adviceWith(context, "quotes", AdviceWithRouteBuilder::mockEndpoints);

        // must start Camel after we are done using advice-with
        context.start();

        getMockEndpoint("mock:seda:camel").expectedBodiesReceived("Camel rocks");
        getMockEndpoint("mock:seda:other").expectedBodiesReceived("Bad donkey");

        template.sendBody("seda:quotes", "Camel rocks");
        template.sendBody("seda:quotes", "Bad donkey");

        assertMockEndpointsSatisfied();
    }
}
