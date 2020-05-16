package net.mefmor.tutorial.spring.boot.camel.tutorial.xml;

import lombok.SneakyThrows;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JaxbDataFormat;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;
import org.xmlunit.assertj.XmlAssert;

import java.io.InputStreamReader;
import java.io.Reader;

import static java.nio.charset.StandardCharsets.UTF_8;


public class JaxbMarshalingTest extends CamelTestSupport {
    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            @Override
            public void configure() {
                JaxbDataFormat jaxb = new JaxbDataFormat();
                jaxb.setContextPath("net.mefmor.tutorial.spring.boot.camel.tutorial.xml");

                from("direct:object")
                        .marshal(jaxb)
                        .to("mock:xml");
            }
        };
    }

    @Test
    void transformObjectToXml() {
        String expectedOutput = asString("expected_client.xml");

        template.sendBody("direct:object", Client.builder().name("Boris").build());

        String actualOutput = getMockEndpoint("mock:xml").getExchanges().get(0).getIn().getBody(String.class);

        XmlAssert.assertThat(actualOutput).and(expectedOutput)
                .ignoreComments().ignoreWhitespace().areIdentical();
    }

    @SneakyThrows
    private String asString(String resourceLocation) {
        return FileCopyUtils.copyToString(new InputStreamReader(JaxbMarshalingTest.class.getResourceAsStream(resourceLocation)));
    }
}
