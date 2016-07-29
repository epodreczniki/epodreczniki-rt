package pl.psnc.ep.rt.web.servlets;

import static pl.psnc.ep.rt.web.Util.getValues;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import pl.psnc.dlibra.common.Info;
import pl.psnc.dlibra.common.InputFilter;
import pl.psnc.dlibra.common.OutputFilter;
import pl.psnc.dlibra.content.ContentServer;
import pl.psnc.dlibra.metadata.AbstractPublicationInfo;
import pl.psnc.dlibra.metadata.Edition;
import pl.psnc.dlibra.metadata.EditionFilter;
import pl.psnc.dlibra.metadata.EditionId;
import pl.psnc.dlibra.metadata.File;
import pl.psnc.dlibra.metadata.FileFilter;
import pl.psnc.dlibra.metadata.FileId;
import pl.psnc.dlibra.metadata.FileInfo;
import pl.psnc.dlibra.metadata.MetadataServer;
import pl.psnc.dlibra.metadata.Publication;
import pl.psnc.dlibra.metadata.PublicationFilter;
import pl.psnc.dlibra.metadata.PublicationId;
import pl.psnc.dlibra.metadata.VersionId;
import pl.psnc.dlibra.metadata.VersionInfo;
import pl.psnc.dlibra.metadata.attributes.AbstractAttributeValue;
import pl.psnc.dlibra.metadata.attributes.AttributeId;
import pl.psnc.dlibra.metadata.attributes.AttributeValue;
import pl.psnc.dlibra.metadata.attributes.AttributeValueSet;
import pl.psnc.dlibra.service.DLibraException;
import pl.psnc.dlibra.service.IdNotFoundException;
import pl.psnc.dlibra.web.comp.resources.ServicesManager;
import pl.psnc.dlibra.web.fw.util.servlet.RequestWrapperFactory;
import pl.psnc.dlibra.web.fw.util.servlet.ServletRequestWrapper;
import pl.psnc.ep.rt.MediaFormat;
import pl.psnc.ep.rt.TargetFormat;
import pl.psnc.ep.rt.WOMIFormat;
import pl.psnc.ep.rt.util.ManualAuthorsHelper;
import pl.psnc.ep.rt.util.Versioning;
import pl.psnc.ep.rt.util.WOMIXMLHandler;
import pl.psnc.ep.rt.web.Util;
import pl.psnc.util.IOUtils;

public class CollXMLServlet extends HttpServlet {

    private static class FutureElement {

        private Element element;
        private HashMap<Element, List<Object>> children = new HashMap<Element, List<Object>>();


        public FutureElement(Element element) {
            this.element = element;
        }


        public Element getElement()
                throws InterruptedException, ExecutionException {
            for (Entry<Element, List<Object>> entry : children.entrySet()) {
                Element element = entry.getKey();
                List<Object> children = entry.getValue();
                for (Object child : children) {
                    if (child instanceof Element) {
                        element.appendChild((Element) child);
                    } else if (child instanceof Future) {
                        Object futureElement = ((Future<?>) child).get();
                        element.appendChild(((FutureElement) futureElement).getElement());
                    } else {
                        throw new RuntimeException("Invalid child object: " + child);
                    }
                }
            }
            return element;
        }


        public void addFutureElement(Element futureParent, Object futureElement) {
            List<Object> list = children.get(futureParent);
            if (list == null)
                children.put(futureParent, list = new ArrayList<Object>());
            list.add(futureElement);
        }
    }


    private static class ContentCreator implements Callable<FutureElement> {

        final Document doc;
        final PublicationId pubId;
        final String repoURL;
        final MetadataServer ms;


        public ContentCreator(Document doc, PublicationId pubId, String repoURL, MetadataServer ms) {
            this.doc = doc;
            this.pubId = pubId;
            this.repoURL = repoURL;
            this.ms = ms;
        }


        @Override
        public FutureElement call()
                throws Exception {
            Element content = element(doc, Namespace.COL, "content", null);
            FutureElement result = new FutureElement(content);
            Collection<Info> childrenInfo = ms.getPublicationManager()
                    .getObjects(new PublicationFilter(null, pubId), new OutputFilter(AbstractPublicationInfo.class))
                    .getResultInfos();
            for (Info childInfo : childrenInfo) {
                AbstractPublicationInfo pubInfo = (AbstractPublicationInfo) childInfo;
                if (pubInfo.getState() != Publication.PUB_STATE_ACTUAL)
                    continue;

                boolean isModule = pubInfo.getStatus() == Publication.PUB_GROUP_LEAF;
                if (isModule) {
                    result.addFutureElement(content,
                        threadPool.submit(new ModuleCreator(doc, pubInfo.getId(), repoURL, ms)));
                } else {
                    Element subcollection = element(doc, Namespace.COL, "subcollection", null);
                    content.appendChild(subcollection);
                    result.addFutureElement(content, subcollection);
                    String title = pubInfo.getLabel();
                    subcollection.appendChild(element(doc, Namespace.MD, "title", title));
                    Element viewAttributes = createViewAttributes(doc, pubInfo.getId(), ms);
                    if (viewAttributes != null)
                        subcollection.appendChild(viewAttributes);
                    result.addFutureElement(subcollection,
                        threadPool.submit(new ContentCreator(doc, pubInfo.getId(), repoURL, ms)));
                }
            }
            return result;
        }
    }


    private static class ModuleCreator extends ContentCreator {

        public ModuleCreator(Document doc, PublicationId pubId, String repoURL, MetadataServer ms) {
            super(doc, pubId, repoURL, ms);
        }


        @Override
        public FutureElement call()
                throws Exception {
            Element module = element(doc, Namespace.COL, "module", null);

            @SuppressWarnings({ "unchecked", "rawtypes" })
            List<Edition> editions = (List) ms.getPublicationManager()
                    .getObjects(new PublicationFilter().setParentIds(Arrays.asList(pubId)),
                        new OutputFilter(Edition.class))
                    .getResults();
            if (editions.size() != 1)
                throw new IdNotFoundException(null, "Publication has " + editions.size() + " editions", pubId);
            Edition edition = editions.get(0);

            AttributeValueSet avs = ms.getElementMetadataManager().getAttributeValueSet(edition.getId(),
                AttributeValue.AV_ASSOC_ALL);
            module.setUserData(KEY_AVS, avs, null);
            List<AbstractAttributeValue> titles = getValues(avs, ms, "Tytul");
            if (!titles.isEmpty())
                module.appendChild(element(doc, Namespace.MD, "title", titles.get(0).getValue()));

            Publication publication = (Publication) ms.getPublicationManager()
                    .getObjects(new PublicationFilter(pubId), new OutputFilter(Publication.class)).getResult();
            FileId mainFileId = publication.getMainFileId();
            File file = (File) ms.getFileManager().getObjects(new FileFilter(mainFileId), new OutputFilter(File.class))
                    .getResult();
            module.setUserData(KEY_FILE, file, null);

            module.setAttribute("repository", repoURL + ModuleServlet.PATH);

            module.setAttribute("document", edition.getExternalId());

            module.setAttribute("version", "1");

            module.setAttributeNS(Namespace.CNXSI.URI, Namespace.CNXSI.prefix() + ":version-at-this-collection-version",
                "1");

            return new FutureElement(module);
        }

    }


    private static enum Namespace {
        COL("http://cnx.rice.edu/collxml"),
        MD("http://cnx.rice.edu/mdml"),
        EP("http://epodreczniki.pl/"),
        CNXSI("http://cnx.rice.edu/system-info");

        public final String URI;


        Namespace(String uri) {
            URI = uri;
        }


        public String prefix() {
            return name().toLowerCase();
        }
    }


    private static ExecutorService threadPool = Executors.newFixedThreadPool(8);

    private static final String KEY_AVS = "avs";

    private static final String KEY_FILE = "file";

    private static final String EP_VERSION = "1.5";

    private static final String TAG_CREATED = "created";

    private static final String TAG_REVISED = "revised";

    private static final Logger logger = Logger.getLogger(CollXMLServlet.class);

    private static ThreadLocal<Integer> versionCache = new ThreadLocal<Integer>();

    private static Map<String, Node> learningObjectives;

    private static ManualAuthorsHelper manualAuthorsHelper = new ManualAuthorsHelper();


    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        try {
            ServletRequestWrapper reqWr = RequestWrapperFactory.getInstance(req, resp);
            PublicationId pubId = getRequestedId(reqWr.getPathInfo(), resp);
            if (pubId == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            Document doc = factory.newDocumentBuilder().newDocument();
            doc.setXmlStandalone(true);

            MetadataServer ms = ServicesManager.getInstance().getMetadataServer();
            Publication rootPub;
            try {
                rootPub = (Publication) ms.getPublicationManager()
                        .getObjects(new PublicationFilter(pubId), new OutputFilter(Publication.class)).getResult();
            } catch (IdNotFoundException e) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            String repoURL = getRepositoryURL(reqWr);
            Element root = createCollection(doc, rootPub, repoURL);
            if (root == null) {
                if (!trySingleObjectCollection(rootPub, resp)) {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                }
                return;
            }

            doc.appendChild(root);
            for (Namespace ns : Namespace.values()) {
                root.setAttribute("xmlns:" + ns.name().toLowerCase(), ns.URI);
            }

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(doc);
            resp.setContentType("application/xml; charset=utf-8");
            StreamResult result = new StreamResult(resp.getOutputStream());
            transformer.transform(source, result);
        } catch (Exception ex) {
            logger.error("Unexpected error occured while accessing server", ex);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }


    private static String getRepositoryURL(ServletRequestWrapper reqWr) {
        String requestUrl = reqWr.getRequestUrl(reqWr.isSecure());
        try {
            URI uri = new URI(requestUrl);
            uri = uri.resolve("/");
            String result = uri.toString();
            if (result.endsWith("/"))
                result = result.substring(0, result.length() - 1);
            return result;
        } catch (URISyntaxException e) {
            throw new RuntimeException("Invalid request url? " + requestUrl);
        }
    }


    private static PublicationId getRequestedId(String requestPathInfo, HttpServletResponse resp)
            throws IOException, DLibraException {
        if (StringUtils.isEmpty(requestPathInfo)) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
        String[] desc = requestPathInfo.split("/");
        if (desc.length != 2 && desc.length != 3) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unproperly formed request.");
            return null;
        }
        try {
            PublicationId id = new PublicationId(Long.valueOf(desc[1]));
            int version = (desc.length == 3) ? Integer.valueOf(desc[2]) : 1;
            versionCache.set(version);
            return Util.findVersion(id, version);
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unproperly formed request.");
            return null;
        }
    }


    private boolean trySingleObjectCollection(Publication publication, HttpServletResponse resp) {
        if (publication.getGroupStatus() != Publication.PUB_NORMAL
                || publication.getState() != Publication.PUB_STATE_ACTUAL)
            return false;

        MetadataServer ms;
        try {
            ms = ServicesManager.getInstance().getMetadataServer();
            Edition edition = (Edition) ms.getPublicationManager()
                    .getObjects(new PublicationFilter(null, publication.getId()), new OutputFilter(Edition.class))
                    .getResult();
            if (edition.getExternalId() != null) {
                return false;
            }

            FileInfo mainFileInfo = (FileInfo) ms.getFileManager()
                    .getObjects(new FileFilter(publication.getMainFileId()), new OutputFilter(FileInfo.class))
                    .getResultInfo();
            if (mainFileInfo.toString().equals(WOMIXMLHandler.MAIN_FILE_NAME))
                return false;

            VersionId mainVersionId = (VersionId) ms.getPublicationManager()
                    .getObjects(new EditionFilter((EditionId) edition.getId()).setMainVersion(true),
                        new OutputFilter(VersionId.class))
                    .getResultId();

            VersionInfo mainVersionInfo = (VersionInfo) ms.getFileManager()
                    .getObjects(new InputFilter(mainVersionId), new OutputFilter(VersionInfo.class)).getResultInfo();
            resp.setContentLength((int) mainVersionInfo.getSize());

            resp.setContentType("text/xml");
            ContentServer cs = ServicesManager.getInstance().getContetServer();
            InputStream fis = cs.getVersionInputStream(mainVersionId);
            try {
                IOUtils.copyStream(fis, resp.getOutputStream());
            } finally {
                fis.close();
                cs.releaseElement(mainVersionId);
            }
            resp.getOutputStream().close();
            return true;
        } catch (Exception e) {
            logger.error("Error while retrieving sinle object collection " + publication.getId(), e);
            return false;
        }
    }


    private static Element element(Document doc, Namespace namespace, String name, String text) {
        Element element = doc.createElementNS(namespace.URI, namespace.prefix() + ":" + name);
        if (text != null) {
            element.setTextContent(text);
        }
        return element;
    }


    private static Element createCollection(Document doc, Publication rootPub, String repoURL)
            throws RemoteException, DLibraException, InterruptedException, ExecutionException {
        Element collection = element(doc, Namespace.COL, "collection", null);

        if (rootPub.getGroupStatus() != Publication.PUB_GROUP_ROOT
                || rootPub.getState() != Publication.PUB_STATE_ACTUAL)
            return null;

        MetadataServer ms = ServicesManager.getInstance().getMetadataServer();
        Element metadata = createMetadata(doc, rootPub, ms, repoURL);
        collection.appendChild(metadata);

        Future<FutureElement> futureContent = threadPool.submit(new ContentCreator(doc, rootPub.getId(), repoURL, ms));
        Element content = futureContent.get().getElement();
        collection.appendChild(content);

        addAuthors(metadata, content, ms);
        addDateTimes(metadata, content);
        return collection;
    }


    private static Element createMetadata(Document doc, Publication pub, MetadataServer ms, String repoURL)
            throws RemoteException, DLibraException {

        Element metadata = element(doc, Namespace.COL, "metadata", null);
        metadata.setAttribute("mdml-version", "1.2");

        AttributeValueSet avs = ms.getElementMetadataManager().getAttributeValueSet(pub.getId(),
            AttributeValue.AV_ASSOC_ALL);
        metadata.setUserData(KEY_AVS, avs, null);
        List<AbstractAttributeValue> rootId = getValues(avs, ms, "RootID");
        String contentId = rootId.isEmpty() ? pub.getId().toString() : rootId.get(0).getValue();
        metadata.appendChild(element(doc, Namespace.MD, "content-id", contentId));
        metadata.appendChild(element(doc, Namespace.MD, "repository", repoURL));
        String version = versionCache.get() != null ? versionCache.get().toString()
                : Versioning.whichVersion(pub.getId(), rootId, ms) + "";
        metadata.appendChild(element(doc, Namespace.MD, "version", version));

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm 'CET'");
        String modTime = df.format(pub.getModificationTime());
        metadata.appendChild(element(doc, Namespace.MD, TAG_CREATED, modTime));
        metadata.appendChild(element(doc, Namespace.MD, TAG_REVISED, modTime));

        List<AbstractAttributeValue> languageAV = getValues(avs, ms, "Jezyk");
        metadata.appendChild(
            element(doc, Namespace.MD, "language", languageAV.isEmpty() ? "pl-PL" : languageAV.get(0).getValue()));

        Element license = element(doc, Namespace.MD, "license", null);
        license.setAttribute("url", "http://creativecommons.org/licenses/by/3.0/pl/legalcode");
        metadata.appendChild(license);

        List<AbstractAttributeValue> titleAV = getValues(avs, ms, "Tytul");
        metadata.appendChild(element(doc, Namespace.MD, "title", titleAV.isEmpty() ? "" : titleAV.get(0).getValue()));

        List<AbstractAttributeValue> subtitleAV = getValues(avs, ms, "Podtytul");
        if (!subtitleAV.isEmpty())
            metadata.appendChild(element(doc, Namespace.MD, "subtitle", subtitleAV.get(0).getValue()));

        List<AbstractAttributeValue> educationLevelListVals = getValues(avs, ms, "EtapEdukacyjny");
        Element educationLevelList = element(doc, Namespace.MD, "education-levellist", null);
        educationLevelList.appendChild(element(doc, Namespace.MD, "education-level",
            educationLevelListVals.isEmpty() ? "" : educationLevelListVals.get(0).getValue()));
        metadata.appendChild(educationLevelList);

        List<AbstractAttributeValue> subjectAV = getValues(avs, ms, "Przedmiot");
        Element subjectlist = element(doc, Namespace.MD, "subjectlist", null);
        for (AbstractAttributeValue subject : subjectAV)
            subjectlist.appendChild(element(doc, Namespace.MD, "subject", subject.getValue()));
        metadata.appendChild(subjectlist);

        List<AbstractAttributeValue> abstractAV = getValues(avs, ms, "Abstrakt");
        metadata.appendChild(
            element(doc, Namespace.MD, "abstract", abstractAV.isEmpty() ? "" : abstractAV.get(0).getValue()));

        Element textbook = element(doc, Namespace.EP, "e-textbook", null);
        textbook.setAttribute("ep:version", EP_VERSION);
        List<AbstractAttributeValue> recipientAV = getValues(avs, ms, "Odbiorca");
        List<AbstractAttributeValue> statusAV = getValues(avs, ms, "StatusTresci");
        List<AbstractAttributeValue> classAV = getValues(avs, ms, "Klasa");
        List<AbstractAttributeValue> volumeAV = getValues(avs, ms, "Tom");
        List<AbstractAttributeValue> stylesheetAV = getValues(avs, ms, "ArkuszStylow");
        List<AbstractAttributeValue> environmentTypeAV = getValues(avs, ms, "SrodowiskoPodrecznika");
        List<AbstractAttributeValue> pubDateAV = getValues(avs, ms, "DataPublikacji");
        List<AbstractAttributeValue> signatureAV = getValues(avs, ms, "Sygnatura");
        List<AbstractAttributeValue> showTechRemarksAV = getValues(avs, ms, "PokazujUwagiTechniczne");
        List<AbstractAttributeValue> learningObjectivesAV = getValues(avs, ms, "CeleKsztalcenia");
        List<AbstractAttributeValue> referencesAV = getValues(avs, ms, "Referencje");
        List<AbstractAttributeValue> editorTypeAV = getValues(avs, ms, "TrybEdytora");

        String recipient = recipientAV.isEmpty() ? null : recipientAV.get(0).getValue();
        String status = statusAV.isEmpty() ? null : statusAV.get(0).getValue();
        textbook.setAttribute("ep:recipient", "Nauczyciel".equals(recipient) ? "teacher" : "student");
        textbook.setAttribute("ep:content-status", "Rozszerzaj\u0105ca".equals(status) ? "expanding"
                : "Uzupe\u0142niaj\u0105ca".equals(status) ? "supplemental" : "canon");
        if (!referencesAV.isEmpty())
            textbook.appendChild(createXMLNode(referencesAV.get(0).getValue(), textbook));
        textbook.appendChild(element(doc, Namespace.EP, "class", classAV.isEmpty() ? "" : classAV.get(0).getValue()));
        if (!volumeAV.isEmpty())
            textbook.appendChild(element(doc, Namespace.EP, "volume", volumeAV.get(0).getValue()));
        textbook.appendChild(
            element(doc, Namespace.EP, "stylesheet", stylesheetAV.isEmpty() ? "" : stylesheetAV.get(0).getValue()));
        textbook.appendChild(element(doc, Namespace.EP, "environment-type",
            environmentTypeAV.isEmpty() ? "normal" : environmentTypeAV.get(0).getValue()));
        if (!pubDateAV.isEmpty())
            textbook.appendChild(
                element(doc, Namespace.EP, "publication-date", pubDateAV.get(0).getValue() + " 12:00 CET"));
        textbook.appendChild(createXMLNode("<ep:signature>"
                + (signatureAV.isEmpty() ? "" : signatureAV.get(0).getValue()) + "</ep:signature>", textbook));
        textbook.appendChild(createCover(doc, avs, ms));
        boolean showTechRemarks = showTechRemarksAV.isEmpty() || !"Nie".equals(showTechRemarksAV.get(0).getValue());
        textbook.appendChild(element(doc, Namespace.EP, "show-technical-remarks", "" + showTechRemarks));
        if (!learningObjectivesAV.isEmpty())
            textbook.appendChild(createLearningObjectives(learningObjectivesAV, doc));
        if (!editorTypeAV.isEmpty())
            textbook.appendChild(element(doc, Namespace.EP, "editor", editorTypeAV.get(0).getValue()));

        metadata.appendChild(textbook);
        return metadata;
    }


    private static Element createViewAttributes(Document doc, PublicationId id, MetadataServer ms)
            throws RemoteException, DLibraException {
        AttributeValueSet avs = ms.getElementMetadataManager().getAttributeValueSet(id, AttributeValue.AV_ASSOC_DIRECT);
        Element panoramaAttribute = null, iconAttribute = null;
        Collection<AbstractAttributeValue> panorama = avs.getAttributeValues(Util.getAttributeId("PanoramaWTle", ms));
        if (panorama != null && !panorama.isEmpty()) {
            panoramaAttribute = element(doc, Namespace.EP, "view-attribute", null);
            panoramaAttribute.setAttribute("ep:type", "panorama");
            panoramaAttribute.setAttribute("ep:id", panorama.iterator().next().getValue());
        }
        Collection<AbstractAttributeValue> icon = avs.getAttributeValues(Util.getAttributeId("IkonaWSpisieTresci", ms));
        if (icon != null && !icon.isEmpty()) {
            iconAttribute = element(doc, Namespace.EP, "view-attribute", null);
            iconAttribute.setAttribute("ep:type", "icon");
            iconAttribute.setAttribute("ep:id", icon.iterator().next().getValue());
        }

        if (panoramaAttribute == null && iconAttribute == null)
            return null;

        Element viewAttributes = element(doc, Namespace.EP, "view-attributes", null);
        if (panoramaAttribute != null)
            viewAttributes.appendChild(panoramaAttribute);
        if (iconAttribute != null)
            viewAttributes.appendChild(iconAttribute);

        return viewAttributes;
    }


    private static Node createXMLNode(String value, Element target) {
        try {
            ByteArrayInputStream xmlStream = new ByteArrayInputStream(value.getBytes("UTF-8"));
            try {
                DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document document = docBuilder.parse(xmlStream);
                Node node = document.getDocumentElement();
                Node imported = target.getOwnerDocument().importNode(node, true);
                return imported;
            } finally {
                xmlStream.close();
            }
        } catch (Exception e) {
            logger.warn("Invalid xml value: " + value, e);
            return element(target.getOwnerDocument(), Namespace.EP, "invalid-xml", value);
        }
    }


    private static void addAuthors(Element metadata, Element content, MetadataServer ms)
            throws RemoteException, DLibraException {
        AttributeId authorAttrId = Util.getAttributeId("Autor", ms);

        AttributeValueSet avs = (AttributeValueSet) metadata.getUserData(KEY_AVS);
        List<AbstractAttributeValue> authorValues = getValues(avs, authorAttrId);
        if (authorValues != null && !authorValues.isEmpty()) {
            String authorsValue = authorValues.get(0).getValue();
            NodeList authorsNodes = manualAuthorsHelper.parseAuthorsValue(authorsValue);
            if (authorsNodes != null) {
                for (int i = 0; i < authorsNodes.getLength(); i++) {
                    Node imported = metadata.getOwnerDocument().importNode(authorsNodes.item(i), true);
                    metadata.appendChild(imported);
                }
                return;
            }
        }

        LinkedHashSet<String> authors = new LinkedHashSet<String>();
        NodeList modules = content.getElementsByTagNameNS(Namespace.COL.URI, "module");
        for (int i = 0; i < modules.getLength(); i++) {
            avs = (AttributeValueSet) modules.item(i).getUserData(KEY_AVS);
            List<AbstractAttributeValue> values = getValues(avs, authorAttrId);
            for (AbstractAttributeValue av : values)
                authors.add(av.getValue());
        }

        Document doc = metadata.getOwnerDocument();
        Element actors = element(doc, Namespace.MD, "actors", null);
        metadata.appendChild(actors);
        Element roles = element(doc, Namespace.MD, "roles", null);
        metadata.appendChild(roles);

        Pattern authorPattern = Pattern.compile("^([^<]+)<([^>]*)>$");
        int id = 1;
        for (String author : authors) {
            String fullName = author;
            String email = "";
            Matcher matcher = authorPattern.matcher(author);
            if (matcher.matches()) {
                fullName = matcher.group(1).trim();
                email = matcher.group(2).trim();
            }
            Element person = element(doc, Namespace.MD, "person", null);
            person.setAttribute("userid", "" + id);
            person.appendChild(element(doc, Namespace.MD, "fullname", fullName));
            person.appendChild(element(doc, Namespace.MD, "firstname",
                fullName.lastIndexOf(' ') > -1 ? fullName.substring(0, fullName.lastIndexOf(' ')) : fullName));
            person.appendChild(element(doc, Namespace.MD, "surname", fullName.lastIndexOf(' ') > -1
                    ? fullName.substring(fullName.lastIndexOf(' ') + 1, fullName.length()) : ""));
            person.appendChild(element(doc, Namespace.MD, "email", email));
            actors.appendChild(person);

            Element role = element(doc, Namespace.MD, "role", "" + id);
            role.setAttribute("type", "author");
            roles.appendChild(role);

            id++;
        }
    }


    private static void addDateTimes(Element metadata, Element content)
            throws RemoteException, DLibraException, InterruptedException, ExecutionException {
        final ContentServer cs = ServicesManager.getInstance().getContetServer();

        final ThreadLocal<DocumentBuilder> documentBuilderTL = new ThreadLocal<DocumentBuilder>() {

            @Override
            protected DocumentBuilder initialValue() {
                try {
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    factory.setNamespaceAware(true);
                    return factory.newDocumentBuilder();
                } catch (ParserConfigurationException e) {
                    throw new RuntimeException("Default parser configuration failed?", e);
                }
            }
        };

        Date created = null, revised = null;
        NodeList modules = content.getElementsByTagNameNS(Namespace.COL.URI, "module");
        List<Future<Document>> parsedModules = new ArrayList<Future<Document>>();

        for (int i = 0; i < modules.getLength(); i++) {
            final File file = (File) modules.item(i).getUserData(KEY_FILE);
            final VersionId versionId = file.getVersionIds().get(0);
            parsedModules.add(threadPool.submit(new Callable<Document>() {

                @SuppressWarnings("resource")
                @Override
                public Document call()
                        throws Exception {
                    InputStream inputStream = cs.getVersionInputStream(versionId);
                    Document module = null;
                    try {
                        try {
                            module = documentBuilderTL.get().parse(inputStream);
                        } finally {
                            inputStream.close();
                        }
                    } catch (Exception e) {
                        logger.warn("Could not parse file version " + versionId, e);
                    } finally {
                        cs.releaseElement(versionId);
                    }
                    return module;
                }
            }));
        }

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm z");
        for (Future<Document> futureModule : parsedModules) {
            Document module = futureModule.get();
            created = findDate(created, df, module, TAG_CREATED, false);
            revised = findDate(revised, df, module, TAG_REVISED, true);
        }

        if (created != null) {
            NodeList nodeList = metadata.getElementsByTagNameNS(Namespace.MD.URI, TAG_CREATED);
            nodeList.item(0).setTextContent(df.format(created));
        }
        if (revised != null) {
            NodeList nodeList = metadata.getElementsByTagNameNS(Namespace.MD.URI, TAG_REVISED);
            nodeList.item(0).setTextContent(df.format(revised));
        }
    }


    private static Date findDate(Date best, SimpleDateFormat df, Document module, String tag, boolean latest) {
        if (module == null)
            return best;
        NodeList createdList = module.getElementsByTagNameNS(Namespace.MD.URI, tag);
        for (int j = 0; j < createdList.getLength(); j++) {
            String dateText = createdList.item(j).getTextContent();
            Date date;
            try {
                date = df.parse(dateText);
            } catch (ParseException e) {
                logger.warn("Could not parse date in file version (" + dateText + ")");
                continue;
            }
            if (best == null || (latest ? date.after(best) : date.before(best)))
                best = date;
        }
        return best;
    }


    private static Element createCover(Document doc, AttributeValueSet avs, MetadataServer ms)
            throws RemoteException, DLibraException {
        List<AbstractAttributeValue> coverAV = getValues(avs, ms, "Okladka");
        Element coverElement = element(doc, Namespace.EP, "cover", coverAV.isEmpty() ? "" : coverAV.get(0).getValue());
        if (coverAV.isEmpty())
            return coverElement;
        try {
            EditionId coverId = new EditionId(Long.valueOf(coverAV.get(0).getValue()));
            Map<WOMIFormat, String> filesMap = Util.loadWOMIData(coverId, ms, null);
            for (Map.Entry<WOMIFormat, String> entry : filesMap.entrySet()) {
                WOMIFormat format = entry.getKey();
                if (format.mediaFormat == MediaFormat.IMAGE && format.targetFormat == TargetFormat.CLASSIC) {
                    String fileName = entry.getValue();
                    int dot = fileName.lastIndexOf('.');
                    String extension = dot >= 0 ? fileName.substring(dot + 1) : fileName;
                    coverElement.setAttribute("ep:cover-type", extension);
                }
            }
        } catch (Exception e) {
            logger.warn("Problem with cover", e);
        }
        return coverElement;
    }


    private static Node createLearningObjectives(List<? extends AbstractAttributeValue> learningObjectivesAV,
            Document targetDocument) {
        if (learningObjectives == null) {
            HashMap<String, Node> lo = new HashMap<String, Node>();
            try {
                InputStream xmlStream = CollXMLServlet.class.getResourceAsStream("/cele_ksztalcenia-opcje.xml");
                try {
                    Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xmlStream);
                    NodeList optionsList = document.getElementsByTagName("option");
                    for (int i = 0; i < optionsList.getLength(); i++) {
                        Node option = optionsList.item(i);
                        String key = findChild(option, "key").getTextContent();
                        Node value = findChild(findChild(option, "value"), "ep:learning-objectives");
                        lo.put(key, value);
                    }
                } finally {
                    xmlStream.close();
                }
                learningObjectives = lo;
            } catch (Exception e) {
                logger.error("Could not parse learning objectives description", e);
            }
        }

        String objective = learningObjectivesAV.get(0).getValue();
        Node objectivesNode = learningObjectives.get(objective);
        if (objectivesNode == null) {
            logger.error("No learning objective description for " + objective);
            return element(targetDocument, Namespace.EP, "learning-objectives", null);
        }
        Node imported = targetDocument.importNode(objectivesNode, true);
        return imported;
    }


    private static Node findChild(Node node, String childName) {
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node item = childNodes.item(i);
            if (childName.equals(item.getNodeName()))
                return item;
        }
        throw new IllegalArgumentException("Missing node " + childName);
    }
}
