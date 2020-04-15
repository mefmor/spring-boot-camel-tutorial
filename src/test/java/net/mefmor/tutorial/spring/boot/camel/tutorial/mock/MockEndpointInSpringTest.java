package net.mefmor.tutorial.spring.boot.camel.tutorial.mock;

import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.CamelSpringTest;
import org.apache.camel.test.spring.junit5.MockEndpoints;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTestContextBootstrapper;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.BootstrapWith;
import org.springframework.test.context.ContextConfiguration;

@CamelSpringTest
@ContextConfiguration
@BootstrapWith(SpringBootTestContextBootstrapper.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@MockEndpoints("log:foo")
class MockEndpointInSpringTest {

    @EndpointInject("mock:log:foo")
    private MockEndpoint log;

    @Produce("direct:start")
    private ProducerTemplate start;

    @BeforeEach
    void setupRoute(@Autowired CamelContext camelContext) throws Exception {
        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() {
                from("direct:start").to("log:foo");
            }
        });
    }

    @Test
    void getLogMessage() {
        log.expectedBodiesReceived("Where's the money Lebowski?");

        start.sendBody("Where's the money Lebowski?");

        log.expectedBodyReceived();
    }
}
