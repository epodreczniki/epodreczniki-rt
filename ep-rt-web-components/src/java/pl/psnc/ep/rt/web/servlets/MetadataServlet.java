package pl.psnc.ep.rt.web.servlets;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import pl.psnc.dlibra.common.OutputFilter;
import pl.psnc.dlibra.metadata.EditionFilter;
import pl.psnc.dlibra.metadata.EditionId;
import pl.psnc.dlibra.metadata.MetadataServer;
import pl.psnc.dlibra.metadata.Publication;
import pl.psnc.dlibra.metadata.attributes.AbstractAttributeValue;
import pl.psnc.dlibra.metadata.attributes.AttributeId;
import pl.psnc.dlibra.metadata.attributes.AttributeValue;
import pl.psnc.dlibra.metadata.attributes.AttributeValueSet;
import pl.psnc.dlibra.service.DLibraException;
import pl.psnc.dlibra.service.IdNotFoundException;
import pl.psnc.dlibra.web.comp.resources.ServicesManager;
import pl.psnc.ep.rt.WOMIFormat;
import pl.psnc.ep.rt.WOMIType;
import pl.psnc.ep.rt.util.ObjectMalformedException;
import pl.psnc.ep.rt.web.Util;

public class MetadataServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(MetadataServlet.class);

    private static final Map<EditionId, WOMIType> womiTypes = new ConcurrentHashMap<EditionId, WOMIType>();

    private static final Map<String, String> attributes = new LinkedHashMap<String, String>();


    static {
        attributes.put("title", "Tytul");
        attributes.put("author", "Autor");
        attributes.put("alternativeText", "TekstAlternatywny");
        attributes.put("license", "Licencja");
        attributes.put("licenseAdditionalInfo", "LicencjaDodatkoweInfo");
        attributes.put("keywords", "SlowaKluczowe");
        attributes.put("purpose", "Przeznaczenie");
        attributes.put("customId", "IdentyfikatorWlasny");
        attributes.put("verificationState", "StanWeryfikacji");
    }


    private static final Map<String, String> extendedAttributes = new LinkedHashMap<String, String>();


    static {
        extendedAttributes.put("recipient", "Odbiorca");
        extendedAttributes.put("origin", "Pochodzenie");
        extendedAttributes.put("category", "Kategoria");
        extendedAttributes.put("learningObjectives", "PodstawaProgramowa");
        extendedAttributes.put("environments", "SystemOperacyjny");
        extendedAttributes.put("description", "Opis");
    }


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

            JSONObject metadata = new JSONObject();
            for (Map.Entry<String, String> entry : attributes.entrySet()) {
                AttributeId attributeId = Util.getAttributeId(entry.getValue(), ms);
                Collection<AbstractAttributeValue> values = avs.getAttributeValues(attributeId);
                metadata.put(entry.getKey(), toJSON(values));
            }
            forceArray(metadata, "keywords");

            metadata.put("womiType", getWOMIType(womiId, ms).toString().toLowerCase());

            metadata.put("verificationNotes", publication.getNotes() != null ? publication.getNotes() : "");

            Object purpose = metadata.get("purpose");
            if (purpose instanceof JSONArray)
                metadata.put("purpose", purpose = ((JSONArray) purpose).get(0));
            if (JSONObject.NULL.equals(purpose))
                metadata.put("purpose", purpose = "epo");

            metadata.put("extended", getExtendedMetadata(avs, ms));

            byte[] content = metadata.toString(1).getBytes("UTF-8");
            resp.setContentLength(content.length);
            resp.setContentType(Util.JSON_CONTENT_TYPE);
            resp.getOutputStream().write(content);
        } catch (ObjectMalformedException e) {
            logger.error("Unexpected error occured while accessing WOMI " + womiId, e);
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } catch (IdNotFoundException e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } catch (DLibraException e) {
            logger.error("Unexpected error occured while accessing server", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }


    private JSONObject getExtendedMetadata(AttributeValueSet avs, MetadataServer ms)
            throws RemoteException, DLibraException {
        JSONObject result = new JSONObject();
        for (Map.Entry<String, String> entry : extendedAttributes.entrySet()) {
            AttributeId attributeId = Util.getAttributeId(entry.getValue(), ms);
            Collection<AbstractAttributeValue> values = avs.getAttributeValues(attributeId);
            result.put(entry.getKey(), toJSON(values));
        }
        Object recipient = result.get("recipient");
        if ("Ucze\u0144".equals(recipient))
            recipient = "student";
        else if ("Nauczyciel".equals(recipient))
            recipient = "teacher";
        else if (!JSONObject.NULL.equals(recipient)) {
            logger.warn("Unexpected recipient for WOMI " + avs.getElementId() + ": " + recipient + " ("
                    + recipient.getClass() + ")");
            recipient = JSONObject.NULL;
        }
        result.put("recipient", recipient);

        splitValues(result, "learningObjectives");
        splitValues(result, "environments");
        return result;
    }


    private void splitValues(JSONObject result, String key) {
        Object learningObjectives = result.get(key);
        if (learningObjectives instanceof String) {
            JSONArray splitValues = new JSONArray(Arrays.asList(((String) learningObjectives).split("\\|")));
            result.put(key, splitValues);
        } else {
            result.put(key, new JSONArray());
            if (!JSONObject.NULL.equals(learningObjectives))
                logger.warn("Unexpected " + key + " for WOMI " + result.get("title") + ": " + learningObjectives + " ("
                        + learningObjectives.getClass() + ")");
        }
    }


    private void forceArray(JSONObject result, String key) {
        Object value = result.get(key);
        if (!(value instanceof JSONArray)) {
            JSONArray array = new JSONArray();
            if (!JSONObject.NULL.equals(value))
                array.put(value);
            result.put(key, array);
        }
    }


    private Object toJSON(Collection<AbstractAttributeValue> values) {
        if (values == null || values.isEmpty()) {
            return JSONObject.NULL;
        } else if (values.size() == 1) {
            return values.iterator().next().getValue();
        } else {
            JSONArray array = new JSONArray();
            for (AbstractAttributeValue v : values)
                array.put(v.getValue());
            return array;
        }
    }


    public static WOMIType getWOMIType(EditionId womiId, MetadataServer ms)
            throws DLibraException, IOException {
        WOMIType womiType = womiTypes.get(womiId);
        if (womiType == null) {
            Map<WOMIFormat, String> womiData = Util.loadWOMIData(womiId, ms,
                ServicesManager.getInstance().getContetServer());
            womiType = womiData.keySet().iterator().next().womiType;
            womiTypes.put(womiId, womiType);
        }
        return womiType;
    }
}
