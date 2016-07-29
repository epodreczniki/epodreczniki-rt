package pl.psnc.ep.rt.web.servlets;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import pl.psnc.dlibra.common.OutputFilter;
import pl.psnc.dlibra.metadata.EditionFilter;
import pl.psnc.dlibra.metadata.EditionId;
import pl.psnc.dlibra.metadata.MetadataServer;
import pl.psnc.dlibra.metadata.Publication;
import pl.psnc.dlibra.metadata.attributes.AbstractAttributeValue;
import pl.psnc.dlibra.metadata.attributes.AttributeFilter;
import pl.psnc.dlibra.metadata.attributes.AttributeId;
import pl.psnc.dlibra.metadata.attributes.AttributeInfo;
import pl.psnc.dlibra.metadata.attributes.AttributeValue;
import pl.psnc.dlibra.metadata.attributes.AttributeValueSet;
import pl.psnc.dlibra.service.DLibraException;
import pl.psnc.dlibra.service.IdNotFoundException;
import pl.psnc.dlibra.web.comp.resources.ServicesManager;
import pl.psnc.ep.rt.server.EPService;
import pl.psnc.ep.rt.util.ObjectMalformedException;
import pl.psnc.ep.rt.web.Util;

public class XmlMetadataServlet extends HttpServlet {

    private static Logger logger = Logger.getLogger(XmlMetadataServlet.class);

    private static List<String> ATTRIBUTES = Arrays.asList("Tytul", "Autor", "TekstAlternatywny", "Licencja",
        "LicencjaDodatkoweInfo", "StanWeryfikacji");

    private static Map<String, AttributeId> rdfNameToAttribute;


    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        EditionId womiId = Util.getRequestedId(req, resp);
        if (womiId == null)
            return;

        try {
            MetadataServer ms = ServicesManager.getInstance().getMetadataServer();
            Publication publication = (Publication) ms.getPublicationManager()
                    .getObjects(new EditionFilter(womiId), new OutputFilter(Publication.class)).getResult();
            if (publication.getState() != Publication.PUB_STATE_ACTUAL) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            AttributeValueSet avs = ms.getElementMetadataManager().getAttributeValueSet(womiId,
                AttributeValue.AV_ASSOC_ALL);

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            Document doc = factory.newDocumentBuilder().newDocument();
            doc.setXmlStandalone(true);
            Element root = doc.createElement("metadata");
            doc.appendChild(root);

            Element idItem = doc.createElement("id");
            idItem.setTextContent(womiId.toString());
            root.appendChild(idItem);

            Element typeItem = doc.createElement("typWOMI");
            typeItem.setTextContent(MetadataServlet.getWOMIType(womiId, ms).toString());
            root.appendChild(typeItem);

            EPService eps = (EPService) ServicesManager.getInstance().getServiceResolver()
                    .getService(EPService.SERVICE_TYPE, null);
            Double videoAspectRatio = eps.getVideoAspectRatio(womiId);
            if (videoAspectRatio != null) {
                Element aspectRatioElement = doc.createElement("aspectRatio");
                aspectRatioElement.setTextContent(String.format("%1.4f", videoAspectRatio));
                root.appendChild(aspectRatioElement);
            }

            Element notesItem = doc.createElement("Notatki");
            notesItem.appendChild(doc.createCDATASection(publication.getNotes() != null ? publication.getNotes() : ""));
            root.appendChild(notesItem);

            for (String attribute : ATTRIBUTES) {
                Collection<AbstractAttributeValue> attributeValues = avs
                        .getAttributeValues(getAttributeId(attribute, ms));
                if (attributeValues == null)
                    continue;
                for (AbstractAttributeValue av : attributeValues) {
                    Element item = doc.createElement(attribute);
                    item.appendChild(doc.createCDATASection(av.getValue()));
                    root.appendChild(item);
                }
            }

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(doc);
            resp.setContentType("text/xml");
            try {
                StreamResult result = new StreamResult(resp.getOutputStream());
                transformer.transform(source, result);
            } finally {
                resp.getOutputStream().close();
            }
        } catch (ObjectMalformedException e) {
            logger.error("Unexpected error occured while accessing WOMI " + womiId, e);
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } catch (IdNotFoundException e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } catch (Exception e) {
            logger.error("Unexpected error occured while generating metadata xml", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }


    private AttributeId getAttributeId(String rdfName, MetadataServer ms)
            throws RemoteException, DLibraException {
        if (rdfNameToAttribute == null) {
            rdfNameToAttribute = new HashMap<String, AttributeId>();
            @SuppressWarnings({ "unchecked", "rawtypes" })
            List<AttributeInfo> infos = (List) ms.getAttributeManager()
                    .getObjects((AttributeFilter) new AttributeFilter(null, null).setRecursive(true),
                        new OutputFilter(AttributeInfo.class))
                    .getResultInfos();
            for (AttributeInfo info : infos) {
                rdfNameToAttribute.put(info.getRDFName(), info.getId());
            }
        }
        return rdfNameToAttribute.get(rdfName);
    }
}
