package net.mefmor.tutorial.spring.boot.camel.tutorial.xml;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JaxbDataFormat;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.Test;
import org.xmlunit.assertj.XmlAssert;


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
        String expectedOutput = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><client name=\"Boris\"/>";

        template.sendBody("direct:object", Client.builder().name("Boris").build());

        String actualOutput = getMockEndpoint("mock:xml").getExchanges().get(0).getIn().getBody(String.class);

        XmlAssert.assertThat(actualOutput).and(expectedOutput)
                .ignoreComments().ignoreWhitespace().areIdentical();
    }
}
