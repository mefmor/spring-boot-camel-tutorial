package net.mefmor.tutorial.spring.boot.camel.tutorial.db;

import org.apache.camel.*;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.language.xpath.XPath;
import org.apache.camel.test.spring.junit5.CamelSpringTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTestContextBootstrapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.BootstrapWith;
import org.springframework.test.context.ContextConfiguration;

import javax.sql.DataSource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CamelSpringTest
@ContextConfiguration
@BootstrapWith(SpringBootTestContextBootstrapper.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class JdbcTest {

    @Produce("direct:start")
    private ProducerTemplate template;

    @EndpointInject("mock:result")
    private MockEndpoint mock;

    @BeforeEach
    void setupRoute(@Autowired CamelContext camelContext) throws Exception {
        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:start")
                        .bean(new OrderToSqlBean())
                        .to("jdbc:dataSource?useHeadersAsParameters=true")
                        .to("mock:result");
            }
        });
    }

    public static class OrderToSqlBean {

        public String toSql(@XPath("order/@name") String name,
                            @XPath("order/@amount") int amount,
                            @XPath("order/@customer") String customer,
                            @Headers Map<String, Object> outHeaders) {
            outHeaders.put("partName", name);
            outHeaders.put("quantity", amount);
            outHeaders.put("customer", customer);
            return "insert into incoming_orders (part_name, quantity, customer) values (:?partName, :?quantity, :?customer)";
        }
    }

    @Test
    void testJdbcInsert(@Autowired JdbcTemplate jdbc) throws Exception {
        mock.expectedMessageCount(1);

        Integer rows = jdbc.queryForObject("select count(*) from incoming_orders", Integer.class);
        assertThat(rows).isEqualTo(0);

        template.sendBody("direct:start", "<order name=\"motor\" amount=\"1\" customer=\"honda\"/>");

        mock.assertIsSatisfied();

        rows = jdbc.queryForObject("select count(*) from incoming_orders", Integer.class);
        assertThat(rows).isEqualTo(1);
    }
}
