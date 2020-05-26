package net.mefmor.tutorial.spring.boot.camel.tutorial.db;

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
class FromDbToJpaObject {
    @Produce("direct:start")
    private ProducerTemplate template;

    @EndpointInject("mock:out")
    private MockEndpoint out;

    @BeforeEach
    void setupRoute(@Autowired CamelContext camelContext, @Autowired CustomerRepository customers) throws Exception {
        customers.save(Customer.builder().id(1L).firstName("Ivan").lastName("Burunov").build());

        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() {
                from("direct:start")
                        .to("jpa:net.mefmor.tutorial.spring.boot.camel.tutorial.db.Customer?resultClass=net.mefmor.tutorial.spring.boot.camel.tutorial.db.Customer&nativeQuery=select * from Customer")
                        .to("mock:out");
            }
        });
    }

    @Test
    void testAddObjectToDatabase() throws InterruptedException {
        out.expectedBodiesReceived(Customer.builder().id(1L).firstName("Ivan").lastName("Burunov").build());

        template.sendBody(null);

        out.assertIsSatisfied();
    }
}
