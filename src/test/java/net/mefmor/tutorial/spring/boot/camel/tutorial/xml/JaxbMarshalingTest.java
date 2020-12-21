package net.mefmor.tutorial.spring.boot.camel.tutorial.xml;

import lombok.SneakyThrows;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JaxbDataFormat;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.util.FileCopyUtils;
import org.xmlunit.assertj.XmlAssert;

import java.io.InputStreamReader;


public class JaxbMarshalingTest extends CamelTestSupport {
    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            @Override
            public void configure() {
                from("direct:object")
                        .marshal().jaxb(Client.class.getPackage().getName())
                        .to("mock:xml");
            }
        };
    }

    @Test
    void transformObjectToXml() throws InterruptedException {
        getMockEndpoint("mock:xml").expectedMessagesMatches((exchange) -> {
            String input = exchange.getIn().getBody(String.class);
            XmlAssert.assertThat(input).and(asString("expected_client.xml"))
                    .ignoreComments().ignoreWhitespace().areIdentical();
            return true;
        });

        template.sendBody("direct:object", Client.builder().name("Boris").build());

        assertMockEndpointsSatisfied();
    }

    @SneakyThrows
    private String asString(String resourceLocation) {
        return FileCopyUtils.copyToString(new InputStreamReader(this.getClass().getResourceAsStream(resourceLocation)));
    }
}
