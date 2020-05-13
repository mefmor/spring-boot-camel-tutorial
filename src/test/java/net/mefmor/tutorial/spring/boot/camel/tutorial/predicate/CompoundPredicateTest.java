package net.mefmor.tutorial.spring.boot.camel.tutorial.predicate;


import org.apache.camel.CamelExecutionException;
import org.apache.camel.Predicate;
import org.apache.camel.builder.PredicateBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class CompoundPredicateTest extends CamelTestSupport {
    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            @Override
            public void configure() {
                Predicate valid = PredicateBuilder.and(
                        xpath("/book/title = 'Camel in Action'"),
                        simple("${header.source} == 'batch'")
                );

                from("direct:start").validate(valid).to("mock:valid");
            }
        };
    }

    @Test
    void testCompoundPredicateValid() throws Exception {
        getMockEndpoint("mock:valid").expectedMessageCount(1);

        String xml = "<book><title>Camel in Action</title></book>";
        template.sendBodyAndHeader("direct:start", xml, "source", "batch");

        assertMockEndpointsSatisfied();
    }

    @Test
    void testCompoundPredicateInvalid() throws Exception {
        getMockEndpoint("mock:valid").expectedMessageCount(0);

        String xml = "Something wrong";
        assertThrows(CamelExecutionException.class,
                () -> template.sendBodyAndHeader("direct:start", xml, "source", "batch"));

        assertMockEndpointsSatisfied();
    }
}
