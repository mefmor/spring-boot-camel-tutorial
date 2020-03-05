package net.mefmor.tutorial.spring.boot.camel.tutorial;

import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
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

@CamelSpringTest
@ContextConfiguration
@BootstrapWith(SpringBootTestContextBootstrapper.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class BeanTransformingTest {
    @Autowired
    private CamelContext context;

    @Produce("direct:xml")
    private ProducerTemplate template;

    @EndpointInject("mock:result")
    private MockEndpoint mock;

    @BeforeEach
    void setupRoute() throws Exception {
        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:xml").bean(new TransformBean()).to("mock:result");
            }
        });
    }

    private static class TransformBean {
        public String transform(String name) {
            return String.format("Hello, %s!", name);
        }
    }

    @Test
    void inputAndOutput() throws InterruptedException {
        mock.expectedBodiesReceived("Hello, Boris!");

        template.sendBody("Boris");

        mock.assertIsSatisfied();
    }
}
