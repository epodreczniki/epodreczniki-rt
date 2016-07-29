package pl.psnc.ep.rt.web.womi;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class GgbToBase64Converter {

    private static final String TEMPLATE = "<article class=\"geogebraweb\" data-param-width=\"{width}\" "
            + "data-param-height=\"{height}\" data-uses-3d=\"{uses3d}\" data-param-ggbbase64=\"{encoded}\"></article>";

    private final byte[] ggb;


    public GgbToBase64Converter(byte[] ggb) {
        this.ggb = ggb;
    }


    public String getHtml()
            throws IOException {
        try {
            byte[] b64 = Base64.encodeBase64(ggb);
            String encoded = new String(b64, "utf-8");
            Size s = getSize();
            String template = TEMPLATE.replace("{width}", s.getWidth()).replace("{height}", s.getHeight())
                    .replace("{uses3d}", s.getUses3d()).replace("{encoded}", encoded);
            return template;
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }


    private Size getSize()
            throws IOException {
        File f = File.createTempFile("epo-womi-ggb-", ".ggb");
        FileUtils.writeByteArrayToFile(f, ggb);
        ZipFile zip = new ZipFile(f);
        ZipEntry entry = zip.getEntry("geogebra.xml");
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        Document doc;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(zip.getInputStream(entry));
            doc.getDocumentElement().normalize();
            XPath xPath = XPathFactory.newInstance().newXPath();

            Element size = findWidthHeightElement(doc, xPath);
            Element uses3d = findUses3dElement(doc, xPath);
            String uses3dVal = uses3d == null ? null : uses3d.getAttribute("val");

            return new Size(size.getAttribute("width"), size.getAttribute("height"), uses3dVal);
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException(e);
        } catch (SAXException e) {
            throw new IllegalStateException(e);
        } catch (XPathExpressionException e) {
            throw new IllegalStateException(e);
        } finally {
            zip.close();
        }
    }


    private Element findUses3dElement(Document doc, XPath xPath)
            throws XPathExpressionException {
        NodeList nodes = (NodeList) xPath.evaluate(".//uses3D", doc.getDocumentElement(), XPathConstants.NODESET);
        if (nodes.getLength() > 0) {
            return (Element) nodes.item(0);
        } else {
            return null;
        }
    }


    private Element findWidthHeightElement(Document doc, XPath xPath)
            throws XPathExpressionException {
        NodeList nodes = (NodeList) xPath.evaluate(".//window", doc.getDocumentElement(), XPathConstants.NODESET);
        if (nodes.getLength() == 0) {
            nodes = (NodeList) xPath.evaluate("./euclidianView/size", doc.getDocumentElement(), XPathConstants.NODESET);
        }
        Element size = (Element) nodes.item(0);
        return size;
    }


    private class Size {

        private static final String USES_3D_DEFAULT = "false";

        private final String width;

        private final String height;

        private final String uses3d;


        public Size(String width, String height, String uses3d) {
            this.width = width;
            this.height = height;
            this.uses3d = uses3d != null ? uses3d : USES_3D_DEFAULT;
        }


        public String getWidth() {
            return width;
        }


        public String getHeight() {
            return height;
        }


        public String getUses3d() {
            return uses3d;
        }

    }

}
