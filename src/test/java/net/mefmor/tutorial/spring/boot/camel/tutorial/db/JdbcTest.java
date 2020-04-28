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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.BootstrapWith;
import org.springframework.test.context.ContextConfiguration;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

@CamelSpringTest
@ContextConfiguration
@BootstrapWith(SpringBootTestContextBootstrapper.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class JdbcTest {
    @Autowired
    private CamelContext camelContext;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbc;

    @Produce("direct:start")
    private ProducerTemplate template;

    @EndpointInject("mock:result")
    private MockEndpoint mock;

    @BeforeEach
    void setupRoute() throws Exception {
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

    @Test
    void testJdbcInsert() throws Exception {
        mock.expectedMessageCount(1);

        Integer rows = jdbc.queryForObject("select count(*) from incoming_orders", Integer.class);
        assertThat(rows).isEqualTo(0);

        template.sendBody("direct:start", "<order name=\"motor\" amount=\"1\" customer=\"honda\"/>");

        mock.assertIsSatisfied();

        rows = jdbc.queryForObject("select count(*) from incoming_orders", Integer.class);
        assertThat(rows).isEqualTo(1);
    }
}
