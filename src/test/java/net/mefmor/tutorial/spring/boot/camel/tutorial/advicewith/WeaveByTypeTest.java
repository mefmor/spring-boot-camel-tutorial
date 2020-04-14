package net.mefmor.tutorial.spring.boot.camel.tutorial.advicewith;

import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.ModelCamelContext;
import org.apache.camel.model.SplitDefinition;
import org.apache.camel.reifier.RouteReifier;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.apache.camel.builder.AggregationStrategies.flexible;

public class WeaveByTypeTest extends CamelTestSupport {

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
                        .to("mock:line")
                        .end()
                        .to("mock:combined");
            }
        };
    }

    @BeforeEach
    void changeRoute() throws Exception {
        ModelCamelContext mcc = context.adapt(ModelCamelContext.class);
        RouteReifier.adviceWith(mcc.getRouteDefinition("quotes"), mcc, new AdviceWithRouteBuilder() {
            @Override
            public void configure() {
                // find the splitter and insert the route snippet before it
                weaveByType(SplitDefinition.class)
                        .before()
                        .filter(body().contains("Donkey"))
                        .transform(simple("${body},Mules cannot do this"));
            }
        });

        context.start();
    }

    @AfterEach
    void reset() {
        resetMocks();
    }

    @Test
    void testWeaveByType() throws Exception {
        getMockEndpoint("mock:line").expectedBodiesReceived("camel rules", "donkey is bad", "mules cannot do this");
        getMockEndpoint("mock:combined").expectedMessageCount(1);
        getMockEndpoint("mock:combined").message(0).body().isInstanceOf(List.class);

        template.sendBody("seda:quotes", "Camel Rules,Donkey is Bad");

        assertMockEndpointsSatisfied();
    }

    @Test
    void testWeaveByTypeWithoutTheDonkeys() throws Exception {
        getMockEndpoint("mock:line").expectedBodiesReceived("beer is good", "whiskey is better");
        getMockEndpoint("mock:combined").expectedMessageCount(1);
        getMockEndpoint("mock:combined").message(0).body().isInstanceOf(List.class);

        template.sendBody("seda:quotes", "Beer is good,Whiskey is better");

        assertMockEndpointsSatisfied();
    }

}