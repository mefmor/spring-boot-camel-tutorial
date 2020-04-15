package net.mefmor.tutorial.spring.boot.camel.tutorial.advicewith;

import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.Test;

public class WeaveByIdTest extends CamelTestSupport {
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
                        .bean("transformer").id("transform")
                        .to("seda:lower");
            }
        };
    }

    @Test
    void testWeaveById() throws Exception {
        AdviceWithRouteBuilder.adviceWith(context, "quotes", a -> {
            a.weaveById("transform").replace().transform().simple("${body.toUpperCase()}");
            a.weaveAddLast().to("mock:result");
        });
        context.start();

        getMockEndpoint("mock:result").expectedBodiesReceived("HELLO CAMEL!");

        template.sendBody("seda:quotes", "Hello Camel!");

        assertMockEndpointsSatisfied();
    }
}
