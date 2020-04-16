package net.mefmor.tutorial.spring.boot.camel.tutorial.components.inmemorymessaging;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.Test;

import static org.apache.camel.builder.SimpleBuilder.simple;
import static org.assertj.core.api.Assertions.assertThat;

class DirectTest extends CamelTestSupport {

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            @Override
            public void configure() {
                from("direct:start").to("mock:end");
            }
        };
    }

    @Test
    void testMiranda() {
        getMockEndpoint("mock:end").returnReplyBody(simple("In the end!"));

        String reply = template.requestBody("direct:start", null, String.class);

        assertThat(reply).isEqualTo("In the end!");
    }
}
