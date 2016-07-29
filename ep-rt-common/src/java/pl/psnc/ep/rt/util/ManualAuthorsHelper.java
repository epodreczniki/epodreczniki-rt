package pl.psnc.ep.rt.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class ManualAuthorsHelper {

    private static final String XSD_RESOURCE_PATH = "/manual-authors-schema.xsd";

    private static final String XML_PREFIX = "<?xml version=\"1.0\" encoding=\"utf-8\"?> <root xmlns=\"http://cnx.rice.edu/mdml\" xmlns:md=\"http://cnx.rice.edu/mdml\""
            + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://cnx.rice.edu/mdml dummy.xsd\">";

    private static final String XML_POSTFIX = "</root>";

    private static final Logger logger = Logger.getLogger(ManualAuthorsHelper.class);

    private final Schema schema;

    private final DocumentBuilderFactory docBuilderFactory;

    private ErrorHandler errorHandler = new ErrorHandler() {

        @Override
        public void warning(SAXParseException exception)
                throws SAXException {
        }


        @Override
        public void fatalError(SAXParseException exception)
                throws SAXException {
            throw new SAXException(exception.getMessage(), exception);
        }


        @Override
        public void error(SAXParseException exception)
                throws SAXException {
            throw new SAXException(exception.getMessage(), exception);
        }
    };


    public ManualAuthorsHelper() {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        try {
            schema = factory.newSchema(new StreamSource(getClass().getResourceAsStream(XSD_RESOURCE_PATH)));
        } catch (SAXException e) {
            throw new IllegalStateException(e);
        }

        docBuilderFactory = DocumentBuilderFactory.newInstance();
        docBuilderFactory.setNamespaceAware(true);
        docBuilderFactory.setSchema(schema);
    }


    public String validateAuthorValue(String xml) {
        if (xml.charAt(0) != '<' || xml.charAt(xml.length() - 1) != '>')
            return "Value is not an XML";

        xml = XML_PREFIX + xml + XML_POSTFIX;
        try {
            Validator validator = schema.newValidator();
            ByteArrayInputStream is = new ByteArrayInputStream(xml.getBytes("UTF-8"));
            validator.validate(new StreamSource(is));
            return null;
        } catch (SAXException e) {
            return e.getMessage();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }


    public NodeList parseAuthorsValue(String value) {
        if (value.charAt(0) != '<' || value.charAt(value.length() - 1) != '>')
            return null;

        String xml = XML_PREFIX + value + XML_POSTFIX;
        try {
            ByteArrayInputStream xmlStream = new ByteArrayInputStream(xml.getBytes("UTF-8"));
            try {
                DocumentBuilder builder = docBuilderFactory.newDocumentBuilder();
                builder.setErrorHandler(errorHandler);
                Document document = builder.parse(xmlStream);
                NodeList nodes = document.getDocumentElement().getChildNodes();
                return nodes;
            } finally {
                xmlStream.close();
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            logger.warn("Could not parse authors xml for textbook (will search for authors the standard way):\n"
                    + e.getMessage() + "\nValue: " + value);
            logger.debug("XML parsing error:", e);
            return null;
        }
    }
}
