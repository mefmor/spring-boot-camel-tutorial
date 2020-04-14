package net.mefmor.tutorial.spring.boot.camel.tutorial;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.apache.camel.util.StringHelper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SimulateRealComponentTest extends CamelTestSupport {

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            @Override
            public void configure() {
                from("jetty://http://localhost:9080/service/order")
                        .transform().message(message -> "ID=" + message.getHeader("id"))
                        .to("mock:miranda")
                        .transform().body(String.class, b -> StringHelper.after(b, "STATUS="));
            }
        };
    }

    @Test
    void testMiranda() throws InterruptedException {
        context.setTracing(true);

        MockEndpoint mock = getMockEndpoint("mock:miranda");
        mock.expectedBodiesReceived("ID=123");
        mock.whenAnyExchangeReceived(e -> e.getIn().setBody("ID=123, STATUS=IN PROGRESS"));

        String out = fluentTemplate.to("http://localhost:9080/service/order?id=123").request(String.class);
        assertThat(out).isEqualTo("IN PROGRESS");

        assertMockEndpointsSatisfied();
    }

}
