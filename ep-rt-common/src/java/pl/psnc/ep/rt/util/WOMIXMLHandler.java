package pl.psnc.ep.rt.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import pl.psnc.ep.rt.WOMIFormat;

public class WOMIXMLHandler {

    public static final String MAIN_FILE_NAME = "multiFormat.xml";

    public static final WOMIFormat STATIC_ALTERNATIVE_FORMAT = new WOMIFormat(null, null);

    private static final String ROOT_ELEMENT = "formats";

    private static final String FORMAT_ELEMENT = "format";

    private static final String FORMAT_ID_ATTRIBUTE = "id";

    private static final String FORMAT_MAINFILE_ATTRIBUTE = "mainFile";

    private static final String ALTERNATIVES_ELEMENT = "alternatives";

    private static final String ALTERNATIVES_EXIST_ATTRIBUTE = "exist";

    private static final Logger logger = Logger.getLogger(WOMIXMLHandler.class);


    public static Map<WOMIFormat, String> saveMultiFormatXML(Map<WOMIFormat, File> sourceFiles, OutputStream target,
            boolean hasAlternatives)
                    throws IOException {
        Map<WOMIFormat, String> desiredPaths = new HashMap<WOMIFormat, String>();

        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element root = doc.createElement(ROOT_ELEMENT);
            doc.appendChild(root);

            for (Map.Entry<WOMIFormat, File> entry : sourceFiles.entrySet()) {
                WOMIFormat womiFormat = entry.getKey();
                if (womiFormat == null || womiFormat.womiType == null || entry.getValue() == null)
                    continue;
                String fileName = entry.getValue().getName();

                Element formatElement = doc.createElement(FORMAT_ELEMENT);
                formatElement.setAttribute(FORMAT_ID_ATTRIBUTE, womiFormat.id);
                formatElement.setAttribute(FORMAT_MAINFILE_ATTRIBUTE, fileName);
                root.appendChild(formatElement);

                desiredPaths.put(womiFormat, toDLibraPath(womiFormat, fileName));
            }

            Element alternativesElement = doc.createElement(ALTERNATIVES_ELEMENT);
            alternativesElement.setAttribute(ALTERNATIVES_EXIST_ATTRIBUTE, Boolean.toString(hasAlternatives));
            root.appendChild(alternativesElement);

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(target);
            transformer.transform(source, result);
        } catch (ParserConfigurationException e) {
            throw new IOException("Could not initialize XML builder", e);
        } catch (TransformerException e) {
            throw new IOException("Error writing XML", e);
        }
        return desiredPaths;
    }


    public static String toDLibraPath(WOMIFormat format, String fileName) {
        return "/" + format.id + "/" + fileName;
    }


    public static Map<WOMIFormat, String> loadMultiFormatXML(InputStream input)
            throws IOException {
        return loadMultiFormatXML(input, false);
    }


    public static Map<WOMIFormat, String> loadMultiFormatXML(InputStream input, boolean includeStaticAlternative)
            throws IOException {
        Document document;
        try {
            document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(input);
        } catch (SAXException e) {
            throw new ObjectMalformedException("Not a valid XML", e);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }

        NodeList list = document.getElementsByTagName(ROOT_ELEMENT);
        if (list.getLength() != 1) {
            throw new ObjectMalformedException("There are " + list.getLength() + " " + ROOT_ELEMENT + " elements.");
        }
        Map<WOMIFormat, String> formatToPath = new LinkedHashMap<WOMIFormat, String>();
        list = document.getElementsByTagName(FORMAT_ELEMENT);
        for (int i = 0; i < list.getLength(); i++) {
            NamedNodeMap attributes = list.item(i).getAttributes();

            String formatId = attributes.getNamedItem(FORMAT_ID_ATTRIBUTE).getTextContent();
            WOMIFormat format;
            try {
                format = WOMIFormat.fromId(formatId);
            } catch (IllegalArgumentException e) {
                logger.error("Unrecognized format id: " + formatId, e);
                continue;
            }

            String mainFile = attributes.getNamedItem(FORMAT_MAINFILE_ATTRIBUTE).getTextContent();
            formatToPath.put(format, mainFile);
        }
        if (formatToPath.isEmpty()) {
            throw new ObjectMalformedException("No content files in WOMI");
        }
        if (includeStaticAlternative) {
            list = document.getElementsByTagName(ALTERNATIVES_ELEMENT);
            if (list.getLength() == 1) {
                NamedNodeMap attributes = list.item(0).getAttributes();
                String exists = attributes.getNamedItem(ALTERNATIVES_EXIST_ATTRIBUTE).getTextContent();
                if (Boolean.TRUE.toString().equals(exists))
                    formatToPath.put(STATIC_ALTERNATIVE_FORMAT, Boolean.TRUE.toString());
            }
        }
        return formatToPath;
    }
}
