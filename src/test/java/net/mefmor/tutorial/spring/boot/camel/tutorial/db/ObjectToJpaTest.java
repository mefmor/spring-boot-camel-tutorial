package net.mefmor.tutorial.spring.boot.camel.tutorial.db;

import org.apache.camel.CamelContext;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.spring.junit5.CamelSpringTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTestContextBootstrapper;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.BootstrapWith;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CamelSpringTest
@ContextConfiguration
@BootstrapWith(SpringBootTestContextBootstrapper.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ObjectToJpaTest {
    @Produce("direct:start")
    private ProducerTemplate template;

    @Autowired
    private CustomerRepository customers;

    @BeforeEach
    void setupRoute(@Autowired CamelContext camelContext) throws Exception {
        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:start")
                        .to("jpa:net.mefmor.tutorial.spring.boot.camel.tutorial.db.Customer");
            }
        });
    }

    @Test
    void testAddObjectToDatabase() {
        assertThat(customers.findByLastName("Ivanov")).size().isEqualTo(0);

        template.sendBody("direct:start", new Customer("Pavel", "Ivanov"));

        assertThat(customers.findByLastName("Ivanov")).size().isEqualTo(1);
    }

    @Test
    void testSaveCustomersToRepository() {
        Customer inputCustomer = new Customer("Grigori", "Petrov");
        customers.save(inputCustomer);

        List<Customer> finedCustomers = customers.findByLastName("Petrov");

        assertThat(finedCustomers).containsOnly(inputCustomer);
    }
}
