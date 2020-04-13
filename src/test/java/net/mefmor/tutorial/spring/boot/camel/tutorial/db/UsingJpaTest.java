package net.mefmor.tutorial.spring.boot.camel.tutorial.db;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UsingJpaTest {
    @Autowired
    private CustomerRepository customers;

    @Test
    void testSaveCustomersToRepository() {
        Customer inputCustomer = new Customer("Pavel", "Ivanov");
        customers.save(inputCustomer);

        List<Customer> finedCustomers = customers.findByLastName("Ivanov");

        assertThat(finedCustomers.get(0)).isEqualTo(inputCustomer);
    }
}
