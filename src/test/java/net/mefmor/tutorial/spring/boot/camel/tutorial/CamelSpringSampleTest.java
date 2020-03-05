package net.mefmor.tutorial.spring.boot.camel.tutorial;

import org.apache.camel.*;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.CamelSpringTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTestContextBootstrapper;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.BootstrapWith;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;

@CamelSpringTest
@ContextConfiguration
@BootstrapWith(SpringBootTestContextBootstrapper.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CamelSpringSampleTest {

    @Autowired
    private CamelContext camelContext;

    @EndpointInject("mock:a")
    private MockEndpoint mockA;

    @EndpointInject("mock:b")
    private MockEndpoint mockB;

    @Produce("direct:start")
    private ProducerTemplate start;

    @BeforeEach
    void setupRoute() throws Exception {
        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:start")
                        .to("mock:a")
                        .transform(simple("Hello ${body}"))
                        .to("mock:b");
            }
        });
    }

    @Test
    void testPositive() throws Exception {
        assertEquals(ServiceStatus.Started, camelContext.getStatus());

        mockA.expectedBodiesReceived("David");
        mockB.expectedBodiesReceived("Hello David");

        start.sendBody("David");

        MockEndpoint.assertIsSatisfied(camelContext);
    }

}
