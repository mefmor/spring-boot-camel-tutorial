package net.mefmor.tutorial.spring.boot.camel.tutorial.advicewith;

import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.Test;

public class ReplaceFromTest extends CamelTestSupport {

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
                from("aws-sqs:quotes").routeId("quotes")
                        .choice()
                        .when(simple("${body} contains 'Camel'"))
                        .to("seda:camel")
                        .otherwise()
                        .to("seda:other");
            }
        };
    }

    @Test
    void testReplaceFromWithEndpoints() throws Exception {
        AdviceWithRouteBuilder.adviceWith(context, "quotes", a -> {
            a.replaceFromWith("direct:hitme");
            a.mockEndpoints("seda:*");
        });

        // must start Camel after we are done using advice-with
        context.start();

        getMockEndpoint("mock:seda:camel").expectedBodiesReceived("Camel rocks");
        getMockEndpoint("mock:seda:other").expectedBodiesReceived("Bad donkey");

        template.sendBody("direct:hitme", "Camel rocks");
        template.sendBody("direct:hitme", "Bad donkey");

        assertMockEndpointsSatisfied();
    }

}
