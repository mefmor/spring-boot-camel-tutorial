package net.mefmor.tutorial.spring.boot.camel.tutorial.advicewith;

import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.apache.camel.builder.AggregationStrategies.flexible;


public class WeaveByToUriTest extends CamelTestSupport {
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
                        .split(body(), flexible().accumulateInCollection(ArrayList.class))
                        .transform(simple("${body.toLowerCase()}"))
                        .to("seda:line")
                        .end()
                        .to("mock:combined");
            }
        };
    }

    @Test
    void testWeaveByToUri() throws Exception {
        AdviceWithRouteBuilder.adviceWith(context, "quotes",
                a -> a.weaveByToUri("seda:line").replace().to("mock:line"));

        context.start();

        getMockEndpoint("mock:line").expectedBodiesReceived("camel rules", "donkey is bad");
        getMockEndpoint("mock:combined").expectedMessageCount(1);
        getMockEndpoint("mock:combined").message(0).body().isInstanceOf(List.class);

        template.sendBody("seda:quotes", "Camel Rules,Donkey is Bad");

        assertMockEndpointsSatisfied();
    }
}
