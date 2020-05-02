package net.mefmor.tutorial.spring.boot.camel.tutorial.db;

import org.apache.camel.CamelContext;
import org.apache.camel.Headers;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
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
import org.springframework.test.context.jdbc.Sql;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CamelSpringTest
@ContextConfiguration
@BootstrapWith(SpringBootTestContextBootstrapper.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Sql("create-table-incoming_orders.sql")
class XmlToJdbcTest {

    @Produce("direct:start")
    private ProducerTemplate template;

    @BeforeEach
    void setupRoute(@Autowired CamelContext camelContext) throws Exception {
        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:start")
                        .bean(new OrderToSqlBean())
                        .to("jdbc:dataSource?useHeadersAsParameters=true");
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
    void testJdbcInsert(@Autowired JdbcTemplate jdbc) {
        assertThat(jdbc.queryForObject("select count(*) from incoming_orders", Integer.class)).isEqualTo(0);

        template.sendBody("direct:start", "<order name=\"motor\" amount=\"1\" customer=\"honda\"/>");

        assertThat(jdbc.queryForObject("select count(*) from incoming_orders", Integer.class)).isEqualTo(1);
    }
}
