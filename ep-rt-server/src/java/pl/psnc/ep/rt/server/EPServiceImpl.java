package pl.psnc.ep.rt.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import pl.psnc.dlibra.common.Info;
import pl.psnc.dlibra.common.InputFilter;
import pl.psnc.dlibra.common.OutputFilter;
import pl.psnc.dlibra.content.ContentServer;
import pl.psnc.dlibra.content.PluginContentInfo;
import pl.psnc.dlibra.content.server.BasicRemoteOutputStream;
import pl.psnc.dlibra.content.server.DigestedElementOutputStream;
import pl.psnc.dlibra.db.HibernateController;
import pl.psnc.dlibra.event.AbstractEvent;
import pl.psnc.dlibra.event.EventConsumer;
import pl.psnc.dlibra.event.metadata.CreatePublicationEvent;
import pl.psnc.dlibra.event.metadata.DeleteElementEvent;
import pl.psnc.dlibra.event.metadata.ModifyElementMetadataEvent;
import pl.psnc.dlibra.metadata.AbstractPublicationInfo;
import pl.psnc.dlibra.metadata.DirectoryFilter;
import pl.psnc.dlibra.metadata.DirectoryInfo;
import pl.psnc.dlibra.metadata.DirectoryManager;
import pl.psnc.dlibra.metadata.Edition;
import pl.psnc.dlibra.metadata.EditionFilter;
import pl.psnc.dlibra.metadata.EditionId;
import pl.psnc.dlibra.metadata.ElementId;
import pl.psnc.dlibra.metadata.FileFilter;
import pl.psnc.dlibra.metadata.MetadataServer;
import pl.psnc.dlibra.metadata.Publication;
import pl.psnc.dlibra.metadata.PublicationFilter;
import pl.psnc.dlibra.metadata.PublicationId;
import pl.psnc.dlibra.metadata.PublicationManager;
import pl.psnc.dlibra.metadata.VersionId;
import pl.psnc.dlibra.metadata.VersionInfo;
import pl.psnc.dlibra.metadata.attributes.AbstractAttributeValue;
import pl.psnc.dlibra.metadata.attributes.AttributeFilter;
import pl.psnc.dlibra.metadata.attributes.AttributeId;
import pl.psnc.dlibra.metadata.attributes.AttributeValue;
import pl.psnc.dlibra.metadata.attributes.AttributeValueSet;
import pl.psnc.dlibra.mgmt.DLStaticServiceResolver;
import pl.psnc.dlibra.mgmt.ServiceResolver;
import pl.psnc.dlibra.service.AbstractService;
import pl.psnc.dlibra.service.DLibraException;
import pl.psnc.dlibra.service.DefaultPasswordChecker;
import pl.psnc.dlibra.service.IdNotFoundException;
import pl.psnc.dlibra.service.ServiceInfo;
import pl.psnc.dlibra.service.conf.ServiceConfigurationBean;
import pl.psnc.dlibra.user.ActorId;
import pl.psnc.dlibra.user.DirectoryRightId;
import pl.psnc.dlibra.user.RightManager;
import pl.psnc.dlibra.user.UserId;
import pl.psnc.dlibra.user.UserServer;
import pl.psnc.ep.rt.MediaFormat;
import pl.psnc.ep.rt.TargetFormat;
import pl.psnc.ep.rt.WOMIFormat;
import pl.psnc.ep.rt.WOMIType;
import pl.psnc.ep.rt.util.MimetypesMapProvider;
import pl.psnc.ep.rt.util.ObjectMalformedException;
import pl.psnc.ep.rt.util.Versioning;
import pl.psnc.ep.rt.util.WOMIXMLHandler;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class EPServiceImpl extends AbstractService<ServiceResolver> implements EPService, EventConsumer {

    private static class WOMIInfo {

        public EditionId womiId;
        public WOMIFormat format;
        public Map<WOMIFormat, String> filesMap;
        public long mediaFileSize;
    }


    private static class ModificationInfo {

        public volatile Date modificationEndGuess;

        public volatile Date lastReportTime;

    }


    private static final WOMIFormat FORMAT_VIDEO = new WOMIFormat(WOMIType.MOVIE, MediaFormat.VIDEO);

    private static final WOMIFormat FORMAT_AUDIO = new WOMIFormat(WOMIType.SOUND, MediaFormat.AUDIO);

    private static final WOMIFormat FORMAT_IMAGE = new WOMIFormat(WOMIType.GRAPHICS, MediaFormat.IMAGE);

    private static final WOMIFormat FORMAT_ICON = new WOMIFormat(WOMIType.ICON, MediaFormat.ICON);

    private static final WOMIFormat FORMAT_INTERACTIVE = new WOMIFormat(WOMIType.INTERACTIVE, MediaFormat.PACKAGE);

    private static List<WOMIFormat> FORMATS = Arrays.asList(FORMAT_IMAGE, FORMAT_ICON, FORMAT_INTERACTIVE, FORMAT_VIDEO,
        FORMAT_AUDIO);

    private static final String CONFIG_RT_CONTENT_URL = "rt.content.url";

    private static final String CONFIG_MESSAGE_QUEUE_URI = "message.queue.uri";

    private static final String CONFIG_MESSAGE_QUEUE_EXCHANGE_NAME = "message.queue.exchange.name";

    private static final String CONFIG_MESSAGE_QUEUE_ROUTING_KEY_PREFIX = "message.queue.routing.key.prefix";

    private static final String CONFIG_MESSAGE_GUESS_MODIFICATION_TIME = "textbook.guess.modification.time";

    private static final String RESOURCE_TEXTBOOK = "collection";

    private static final String RESOURCE_WOMI = "womi";

    private static final String RESOURCE_MODULE = "module";

    private static final String COMMAND_ADDED = "added";

    private static final String COMMAND_DELETED = "deleted";

    private static final String COMMAND_ACCEPTED = "accepted";

    private static final String COMMAND_MODIFIED = "modified";

    private static final String CONFIG_AV_REPO_URL = "av.repository.url";

    private static final String AV_ADDED = "UpdateMaterial";

    private static final String AV_REMOVED = "RemoveMaterial";

    private static final String AV_GET_STATUS = "GetStatus";

    private static final String AV_GET_FORMATS = "GetMaterialFormats";

    private static final Logger logger = Logger.getLogger(EPServiceImpl.class);

    private static final Map<PublicationId, ModificationInfo> textbookModificationReports = new ConcurrentHashMap<PublicationId, ModificationInfo>();

    private static final Map<String, Date> moduleModificationReports = new ConcurrentHashMap<String, Date>();

    private static final Set<PublicationId> newObjects = new HashSet<PublicationId>();

    private long textbookModificationTimeGuess = 10 * 60 * 1000;

    private HttpClient httpClient;


    public EPServiceImpl()
            throws RemoteException {
        super();
    }


    @Override
    public void setConfiguration(ServiceResolver serviceResolver, ServiceConfigurationBean sc,
            ServiceInfo loggedServiceInfo_, UserId userId_)
                    throws RemoteException, DLibraException {
        super.setConfiguration(serviceResolver, sc, loggedServiceInfo_, userId_);

        String time = serviceConf.getProperty(CONFIG_MESSAGE_GUESS_MODIFICATION_TIME);
        try {
            textbookModificationTimeGuess = Long.valueOf(time);
        } catch (Exception e) {
        }
    }


    @Override
    protected void pushLocal(AbstractEvent[] events)
            throws RemoteException, DLibraException {
        ArrayList<Edition> modulesDeleted = new ArrayList<Edition>();
        ArrayList<Edition> womisDeleted = new ArrayList<Edition>();
        for (AbstractEvent event : events) {
            if (event instanceof DeleteElementEvent) {
                ElementId elementId = ((DeleteElementEvent) event).getElementId();
                if (elementId instanceof EditionId) {
                    Edition edition = getEdition((EditionId) elementId);
                    if (edition.getExternalId() != null) {
                        reportChange(edition.getExternalId(), RESOURCE_MODULE, COMMAND_DELETED);
                        modulesDeleted.add(edition);
                    } else {
                        womisDeleted.add(edition);
                        reportChange(edition.getParentId(), RESOURCE_TEXTBOOK, COMMAND_DELETED);
                    }
                } else if (elementId instanceof PublicationId) {
                    if (elementId.equals(getGroupRoot((PublicationId) elementId))) {
                        reportChange(elementId, RESOURCE_TEXTBOOK, COMMAND_DELETED);
                    }
                }
            } else if (event instanceof CreatePublicationEvent) {
                CreatePublicationEvent cpEvent = (CreatePublicationEvent) event;
                PublicationId publicationId = (PublicationId) cpEvent.getElementId();
                if (cpEvent.getStatus() == Publication.PUB_GROUP_ROOT) {
                    ModificationInfo modificationInfo = textbookModificationReports.get(publicationId);
                    if (modificationInfo == null) {
                        modificationInfo = new ModificationInfo();
                        textbookModificationReports.put(publicationId, modificationInfo);
                    }
                    if (modificationInfo.lastReportTime == null
                            || modificationInfo.lastReportTime.before(event.getTimestamp())) {
                        modificationInfo.modificationEndGuess = new Date(
                                event.getTimestamp().getTime() + textbookModificationTimeGuess);
                    }
                } else if (cpEvent.getStatus() != Publication.PUB_GROUP_MID) {
                    newObjects.add(publicationId);
                }
            } else if (event instanceof ModifyElementMetadataEvent) {
                ElementId elementId = ((ModifyElementMetadataEvent) event).getElementId();
                if (elementId instanceof PublicationId) {
                    PublicationId groupRoot = getGroupRoot((PublicationId) elementId);
                    if (groupRoot != null) {
                        textBookModified(event.getTimestamp(), groupRoot);
                    }
                } else if (elementId instanceof EditionId) {
                    reportObjectModified((EditionId) elementId, event.getTimestamp(), false);
                }
            }
        }

        try {
            sendAudioVideoRemovedReport(womisDeleted);
        } catch (Exception e) {
            logger.error("Communication to AV Repository failed", e);
        }

        for (Edition edition : womisDeleted) {
            reportChange(edition.getId(), RESOURCE_WOMI, COMMAND_DELETED);
        }

        removeExternalIds(modulesDeleted);
    }


    private void textBookModified(Date eventTime, PublicationId groupRoot) {
        ModificationInfo modificationInfo = textbookModificationReports.get(groupRoot);
        if (modificationInfo != null) {
            if (modificationInfo.lastReportTime != null && modificationInfo.lastReportTime.after(eventTime))
                return;
            if (modificationInfo.modificationEndGuess != null
                    && modificationInfo.modificationEndGuess.after(eventTime)) {
                modificationInfo.modificationEndGuess = new Date(eventTime.getTime() + textbookModificationTimeGuess);
                return;
            }
        } else {
            modificationInfo = new ModificationInfo();
            textbookModificationReports.put(groupRoot, modificationInfo);
        }

        textbookModificationEnd(groupRoot, false);
    }


    private void sendAudioVideoAddedReport(WOMIInfo avInfo)
            throws IOException, DLibraException {
        String repoURL = serviceConf.getProperty(CONFIG_AV_REPO_URL);
        if (repoURL == null || repoURL.isEmpty())
            return;

        if (avInfo.mediaFileSize == 0) {
            logger.info("Skipping notification to AV Repository: WOMI " + avInfo.womiId + " has 0 size.");
            return;
        }

        HttpPut request = new HttpPut(repoURL + AV_ADDED);

        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("materialId", avInfo.womiId.toString()));
        String materialURL = getURL(avInfo, avInfo.format);
        nameValuePairs.add(new BasicNameValuePair("materialURL", materialURL));

        String fileName = avInfo.filesMap.get(avInfo.format).toLowerCase();
        String contentType = MimetypesMapProvider.get().getContentType(fileName);
        if (fileName.endsWith(".mp4") && avInfo.format == FORMAT_AUDIO)
            contentType = "audio/mp4";
        nameValuePairs.add(new BasicNameValuePair("mime", contentType));

        String metadata = getAudioVideoMetadata(avInfo);
        nameValuePairs.add(new BasicNameValuePair("metadata", metadata));
        request.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));

        logger.info("Reporting new/modified audio/video WOMI to AV Repository");
        logger.info("sent metadata: " + metadata);
        HttpResponse response = getHttpClient().execute(request);
        int status = response.getStatusLine().getStatusCode();
        if (status != HttpStatus.SC_OK && status != HttpStatus.SC_NO_CONTENT) {
            String body = response.getEntity() != null ? EntityUtils.toString(response.getEntity()) : null;
            logger.error("Server response: " + body);
            EntityUtils.consume(response.getEntity());
            throw new IOException("Unexpected AV Repository status: " + status);
        }
        EntityUtils.consume(response.getEntity());
    }


    private String getURL(WOMIInfo avInfo, WOMIFormat format) {
        URI materialURI = URI.create(serviceConf.getProperty(CONFIG_RT_CONTENT_URL));
        try {
            String fileName = avInfo.filesMap.get(format);
            materialURI = materialURI
                    .resolve(new URI(null, null, avInfo.womiId + WOMIXMLHandler.toDLibraPath(format, fileName), null));
        } catch (URISyntaxException e) {
            throw new RuntimeException("This uri can't be invalid", e);
        }
        return materialURI.toString();
    }


    private void sendAudioVideoRemovedReport(List<Edition> movies)
            throws IOException {
        String repoURL = serviceConf.getProperty(CONFIG_AV_REPO_URL);
        if (repoURL == null || repoURL.isEmpty())
            return;
        for (Edition edition : movies) {
            HttpDelete request = new HttpDelete(repoURL + AV_REMOVED + "/" + edition.getId());
            HttpResponse response = getHttpClient().execute(request);
            int status = response.getStatusLine().getStatusCode();
            if (status != HttpStatus.SC_NO_CONTENT && status != HttpStatus.SC_OK && status != HttpStatus.SC_NOT_FOUND) {
                logger.error("Server response: " + EntityUtils.toString(response.getEntity()));
                EntityUtils.consume(response.getEntity());
                throw new IOException("Unexpected Converter Server status: " + status);
            }
            EntityUtils.consume(response.getEntity());
        }
    }


    private synchronized HttpClient getHttpClient() {
        if (httpClient == null) {
            ThreadSafeClientConnManager ccm = new ThreadSafeClientConnManager();
            httpClient = new DefaultHttpClient(ccm);
        }
        return httpClient;
    }


    private Edition getEdition(EditionId editionId)
            throws RemoteException, DLibraException {
        MetadataServer ms = DLStaticServiceResolver.getMetadataServer(getSr(), null);
        Edition edition = (Edition) ms.getPublicationManager()
                .getObjects(new EditionFilter(editionId), new OutputFilter(Edition.class)).getResult();
        return edition;
    }


    private WOMIInfo getWOMIInfo(EditionId editionId)
            throws RemoteException, DLibraException {
        MetadataServer ms = DLStaticServiceResolver.getMetadataServer(getSr(), null);
        VersionId mainId = (VersionId) ms.getPublicationManager()
                .getObjects(new EditionFilter(editionId).setMainVersion(true), new OutputFilter(VersionId.class))
                .getResultId();
        ContentServer cs = DLStaticServiceResolver.getContentServer(getSr(), null);
        InputStream fis = cs.getVersionInputStream(mainId);
        try {
            try {
                Map<WOMIFormat, String> filesMap = WOMIXMLHandler.loadMultiFormatXML(fis);

                WOMIInfo avInfo = new WOMIInfo();
                avInfo.womiId = editionId;
                avInfo.filesMap = filesMap;
                for (WOMIFormat format : FORMATS) {
                    String fileName = filesMap.get(format);
                    if (fileName == null)
                        continue;
                    if (avInfo.format != null) {
                        logger.error(
                            "Too many media formats in WOMI " + editionId + ": " + avInfo.format + " + " + format);
                        return null;
                    }
                    avInfo.format = format;

                    FileFilter fileFilter = new FileFilter(WOMIXMLHandler.toDLibraPath(format, fileName));
                    fileFilter.setEditionId(editionId);
                    VersionId mediaId = (VersionId) ms.getFileManager()
                            .getObjects(fileFilter, new OutputFilter(VersionId.class)).getResultId();
                    VersionInfo mediaInfo = (VersionInfo) ms.getFileManager()
                            .getObjects(new InputFilter(mediaId), new OutputFilter(VersionInfo.class)).getResultInfo();
                    avInfo.mediaFileSize = mediaInfo.getSize();
                }
                if (avInfo.format == null) {
                    logger.error("Could not determine format of WOMI " + editionId);
                    return null;
                }
                return avInfo;
            } catch (ObjectMalformedException e) {
                logger.warn("Could not parse WOMI " + editionId + "description:" + e.getMessage());
                return null;
            } finally {
                fis.close();
                cs.releaseElement(mainId);
            }
        } catch (IOException e) {
            logger.debug("I/O Error while parsing WOMI", e);
            return null;
        }
    }


    private String getAudioVideoMetadata(WOMIInfo avInfo)
            throws IOException, DLibraException {
        MetadataServer ms = DLStaticServiceResolver.getMetadataServer(getSr(), null);
        AttributeValueSet avs = ms.getElementMetadataManager().getAttributeValueSet(avInfo.womiId,
            AttributeValue.AV_ASSOC_ALL);
        AttributeId attributeId = (AttributeId) ms.getAttributeManager()
                .getObjects(new AttributeFilter((AttributeId) null).setRDFNames(Arrays.asList("Tytul")),
                    new OutputFilter(AttributeId.class))
                .getResultId();
        List<AbstractAttributeValue> titles = avs.getAttributeValues(attributeId, "pl",
            AttributeValueSet.Values.OnlyDirect);

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            Document doc = factory.newDocumentBuilder().newDocument();
            doc.setXmlStandalone(true);
            Element root = doc.createElementNS("http://epodreczniki.pl/", "metadata");
            doc.appendChild(root);
            Element title = doc.createElement("title");
            title.setTextContent(titles.isEmpty() ? getEdition(avInfo.womiId).getName() : titles.get(0).getValue());
            root.appendChild(title);
            if (avInfo.format == FORMAT_AUDIO) {
                WOMIFormat description = new WOMIFormat(WOMIType.SOUND, MediaFormat.DESCRIPTION);
                if (avInfo.filesMap.containsKey(description)) {
                    Element descriptionURL = doc.createElement("descriptionURL");
                    descriptionURL.setTextContent(getURL(avInfo, description));
                    root.appendChild(descriptionURL);
                }
            } else {
                WOMIFormat subtitles = new WOMIFormat(WOMIType.MOVIE, MediaFormat.SUBTITLES);
                if (avInfo.filesMap.containsKey(subtitles)) {
                    Element subtitlesURL = doc.createElement("subtitlesURL");
                    subtitlesURL.setTextContent(getURL(avInfo, subtitles));
                    root.appendChild(subtitlesURL);
                }
                WOMIFormat captions = new WOMIFormat(WOMIType.MOVIE, MediaFormat.CAPTIONS);
                if (avInfo.filesMap.containsKey(captions)) {
                    Element captionsURL = doc.createElement("captionsURL");
                    captionsURL.setTextContent(getURL(avInfo, captions));
                    root.appendChild(captionsURL);
                }
                WOMIFormat chapters = new WOMIFormat(WOMIType.MOVIE, MediaFormat.CHAPTERS);
                if (avInfo.filesMap.containsKey(chapters)) {
                    Element chaptersURL = doc.createElement("chaptersURL");
                    chaptersURL.setTextContent(getURL(avInfo, chapters));
                    root.appendChild(chaptersURL);
                }
            }

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "no");
            DOMSource source = new DOMSource(doc);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            transformer.transform(source, result);
            return writer.toString();
        } catch (ParserConfigurationException e) {
            throw new IOException("Could not initialize XML builder", e);
        } catch (TransformerException e) {
            throw new IOException("Error writing XML", e);
        }
    }


    private PublicationId getGroupRoot(PublicationId publicationId)
            throws RemoteException, DLibraException {
        PublicationManager pm = DLStaticServiceResolver.getMetadataServer(getSr(), null).getPublicationManager();
        PublicationId root = (PublicationId) pm
                .getObjects(new PublicationFilter(publicationId).setGroupStatus(Publication.PUB_GROUP_ROOT),
                    new OutputFilter(PublicationId.class))
                .getResultId();
        if (root == null) {
            Publication pub = (Publication) pm
                    .getObjects(new PublicationFilter(publicationId), new OutputFilter(Publication.class)).getResult();
            if (pub.getGroupStatus() == Publication.PUB_GROUP_ROOT)
                root = publicationId;
        }
        return root;
    }


    private void removeExternalIds(ArrayList<Edition> editions)
            throws RemoteException, DLibraException {
        try {
            PublicationManager pm = DLStaticServiceResolver.getMetadataServer(getSr(), null).getPublicationManager();
            for (Edition edition : editions) {
                DefaultPasswordChecker.setUser(edition.getLastModifiedBy().getId());
                edition.setExternalId(null);
                pm.setEditionData(edition);
            }
        } finally {
            DefaultPasswordChecker.setUser(null);
        }
    }


    @Override
    public void reportTextbookCreated(PublicationId publicationId) {
        textbookModificationEnd(publicationId, true);
    }


    @Override
    public void textbookModificationStart(PublicationId publicationId) {
        ModificationInfo modificationInfo = textbookModificationReports.get(publicationId);
        if (modificationInfo == null) {
            modificationInfo = new ModificationInfo();
            textbookModificationReports.put(publicationId, modificationInfo);
        }
        modificationInfo.modificationEndGuess = new Date(System.currentTimeMillis() + textbookModificationTimeGuess);
    }


    @Override
    public void textbookModificationEnd(PublicationId publicationId, boolean created) {
        ModificationInfo modificationInfo = textbookModificationReports.get(publicationId);
        if (modificationInfo == null) {
            modificationInfo = new ModificationInfo();
            textbookModificationReports.put(publicationId, modificationInfo);
        }
        modificationInfo.lastReportTime = new Date();
        modificationInfo.modificationEndGuess = null;
        try {
            reportModulesModified(publicationId, created, new Date(),
                DLStaticServiceResolver.getMetadataServer(getSr(), null).getPublicationManager());
        } catch (Exception e) {
            logger.error("Error while reporting changed modules of textbook " + publicationId, e);
        }
        reportChange(publicationId, RESOURCE_TEXTBOOK, created ? COMMAND_ADDED : COMMAND_MODIFIED);
    }


    private void reportModulesModified(PublicationId publicationId, boolean created, Date timestamp,
            PublicationManager pm)
                    throws RemoteException, DLibraException {
        Collection<Info> childrenInfo = pm
                .getObjects(new PublicationFilter(null, publicationId), new OutputFilter(AbstractPublicationInfo.class))
                .getResultInfos();
        for (Info childInfo : childrenInfo) {
            AbstractPublicationInfo pubInfo = (AbstractPublicationInfo) childInfo;
            if (pubInfo.getState() != Publication.PUB_STATE_ACTUAL)
                continue;

            boolean isModule = pubInfo.getStatus() == Publication.PUB_GROUP_LEAF;
            if (isModule) {
                @SuppressWarnings({ "unchecked", "rawtypes" })
                List<EditionId> editionIds = (List) pm
                        .getObjects(new PublicationFilter().setParentIds(Arrays.asList(pubInfo.getId())),
                            new OutputFilter(EditionId.class))
                        .getResultIds();
                if (editionIds.size() != 1)
                    throw new IdNotFoundException(null, "Publication has " + editionIds.size() + " editions",
                            pubInfo.getId());
                reportObjectModified(editionIds.get(0), timestamp, false);
            } else {
                reportModulesModified(pubInfo.getId(), created, timestamp, pm);
            }
        }
    }


    @Override
    public void reportTextbookAccepted(PublicationId publicationId) {
        reportChange(publicationId, RESOURCE_TEXTBOOK, COMMAND_ACCEPTED);
    }


    @Override
    public void reportTextbookRemoved(PublicationId publicationId)
            throws RemoteException {
        reportChange(publicationId, RESOURCE_TEXTBOOK, COMMAND_DELETED);
    }


    @Override
    public void reportWOMIModified(EditionId womiId)
            throws RemoteException, DLibraException {
        reportObjectModified(womiId, new Date(), true);
    }


    private void reportObjectModified(EditionId objectId, Date eventTimestamp, boolean changedContent)
            throws RemoteException, DLibraException {
        Edition object = getEdition(objectId);
        final boolean isNew = newObjects.remove(object.getParentId());
        if (object.getExternalId() != null) {
            Date lastReport = moduleModificationReports.get(object.getExternalId());
            if (lastReport != null && lastReport.after(eventTimestamp))
                return;
            moduleModificationReports.put(object.getExternalId(), new Date());
            reportChange(object.getExternalId(), RESOURCE_MODULE, isNew ? COMMAND_ADDED : COMMAND_MODIFIED);
            return;
        }

        try {
            WOMIInfo womiInfo = getWOMIInfo(objectId);
            if (womiInfo == null) {
                textBookModified(new Date(), (PublicationId) object.getParentId());
                return;
            }
            reportChange(objectId, RESOURCE_WOMI, COMMAND_MODIFIED);
            if (changedContent || isNew) {
                if (womiInfo.format == FORMAT_VIDEO || womiInfo.format == FORMAT_AUDIO) {
                    sendAudioVideoAddedReport(womiInfo);
                }
                if (womiInfo.format == FORMAT_IMAGE || womiInfo.format == FORMAT_ICON) {
                    prefetchImages(womiInfo);
                }
            }
        } catch (IOException e) {
            logger.error("Could not send object change report (" + objectId + ")", e);
        } catch (IdNotFoundException e) {
            logger.warn("Object malformed or removed before events have been handled: " + objectId, e);
        }
    }


    private void prefetchImages(WOMIInfo womiInfo)
            throws DLibraException, IOException {
        ContentServer cs = DLStaticServiceResolver.getContentServer(getSr(), null);
        for (TargetFormat targetFormat : Arrays.asList(TargetFormat.EBOOK, TargetFormat.PDF)) {
            List<Object> parameters = new ArrayList<Object>();
            parameters.add(targetFormat.toString());
            parameters.add(targetFormat.getResolutions()[0]);
            parameters.add(false);
            parameters.add(false);
            PluginContentInfo ci = new PluginContentInfo(womiInfo.womiId, "imagick", parameters);

            ByteArrayOutputStream arrayStream = new ByteArrayOutputStream(1024 * 256);
            DigestedElementOutputStream elementOutputStream = new DigestedElementOutputStream(
                    new BasicRemoteOutputStream(arrayStream, false));
            try {
                cs.getContent(ci, elementOutputStream);
            } finally {
                elementOutputStream.close();
            }
        }
    }


    private void reportChange(Object elementId, String resource, String command) {
        try {
            logger.info("Sending report: " + resource + " " + elementId + "(" + elementId.getClass().getSimpleName()
                    + ") " + command);
            String queueUri = serviceConf.getProperty(CONFIG_MESSAGE_QUEUE_URI);
            if (queueUri == null || queueUri.isEmpty())
                return;
            String exchangeName = serviceConf.getProperty(CONFIG_MESSAGE_QUEUE_EXCHANGE_NAME, "ex.message");
            String routingKeyPrefix = serviceConf.getProperty(CONFIG_MESSAGE_QUEUE_ROUTING_KEY_PREFIX,
                "rt.production.");

            ConnectionFactory factory = new ConnectionFactory();
            factory.setUri(queueUri);
            Connection connection = factory.newConnection();

            JSONObject message = new JSONObject();
            if (resource == RESOURCE_WOMI) {
                message.put("womi_id", elementId);
            } else if (resource == RESOURCE_TEXTBOOK) {
                long[] contentAndVersion = Versioning.findContentIdAndVersion((PublicationId) elementId,
                    DLStaticServiceResolver.getMetadataServer(getSr(), null));
                message.put("collection_id", contentAndVersion[0]);
                message.put("collection_version", contentAndVersion[1]);
            } else if (resource == RESOURCE_MODULE) {
                message.put("module_id", elementId);
                message.put("module_version", 1L);
            } else {
                logger.error("Unknown resource type: " + resource);
            }

            try {
                Channel channel = connection.createChannel();

                String routingKey = routingKeyPrefix + resource + "." + command;

                BasicProperties props = new BasicProperties.Builder().contentType("application/json").build();

                channel.basicPublish(exchangeName, routingKey, props, message.toString().getBytes());
                channel.close();
            } finally {
                connection.close();
            }
        } catch (Exception e) {
            logger.error("Could not send change report: " + resource + " " + elementId + " " + command, e);
        }
    }


    @Override
    public AVInfo getAVInfo(EditionId womiId)
            throws RemoteException, DLibraException {
        try {
            Document document = getAVDocument(womiId, AV_GET_STATUS);
            NodeList nodeList = document.getElementsByTagName("status");
            if (nodeList.getLength() != 1) {
                logger.error("Unexpected xml schema for AV womi " + womiId);
                throw new IOException("Invalid AV Repository response");
            }
            String materialStatus = nodeList.item(0).getTextContent();
            if ("New".equals(materialStatus) || "Recoding".equals(materialStatus) || "Updating".equals(materialStatus))
                return null;
            if (!"Recoded".equals(materialStatus)) {
                logger.error("Unexpected material status: " + materialStatus);
                throw new IOException("Unexpected material status: " + materialStatus);
            }

            AVInfo avInfo = new AVInfo();
            String repoURL = serviceConf.getProperty(CONFIG_AV_REPO_URL);
            URI accessURI = new URI(repoURL).resolve("../../RepositoryAccess/" + womiId);
            if (checkHead(accessURI + "(,,mp4_low_bl)")) {
                avInfo.isVideo = true;
                avInfo.hasAudiodescription = checkHead(accessURI + "(1,,mp4_low_bl)");
                avInfo.hasCaptions = checkHead(accessURI + "(,captions,mp4_low_bl)");
                avInfo.hasSubtitles = checkHead(accessURI + "(,subtitles,mp4_low_bl)");
            } else {
                if (!checkHead(accessURI + "(,,audio_low_aac)")) {
                    logger.error("Material " + womiId + " is neither video nor audio?");
                    throw new RemoteException("Material is not available");
                }
            }
            return avInfo;
        } catch (Exception e) {
            logger.error("Could not get status of AV material: " + womiId, e);
            throw new RemoteException("Unexpected error", e);
        }
    }


    @Override
    public Double getVideoAspectRatio(EditionId womiId)
            throws RemoteException {
        Document document;
        try {
            document = getAVDocument(womiId, AV_GET_FORMATS);
        } catch (Exception e) {
            return null;
        }
        try {
            NodeList nodeList = document.getElementsByTagName("Frame");
            if (nodeList.getLength() == 0) {
                logger.error(AV_GET_FORMATS + ": no frame tag for womi " + womiId);
                return null;
            }
            nodeList = nodeList.item(0).getChildNodes();
            if (nodeList.getLength() != 2) {
                logger.error(
                    AV_GET_FORMATS + ": frame tag for womi " + womiId + " has " + nodeList.getLength() + " children");
                return null;
            }
            int width = 0, height = 0;
            for (int i = 0; i < 2; i++) {
                Node node = nodeList.item(i);
                if (node.getNodeName().equals("Width")) {
                    width = Integer.valueOf(node.getTextContent());
                } else if (node.getNodeName().equals("Height")) {
                    height = Integer.valueOf(node.getTextContent());
                }
            }
            if (width == 0 || height == 0) {
                logger.error(
                    AV_GET_FORMATS + ": invalid dimensions for womi " + womiId + " (" + width + "x" + height + ")");
                return null;
            }
            Double aspectRatio = 1.0 * width / height;
            return aspectRatio;
        } catch (Exception e) {
            logger.error("Could not read aspect ratio", e);
            return null;
        }
    }


    private Document getAVDocument(EditionId womiId, String apiCommand)
            throws RemoteException, IOException, ClientProtocolException, SAXException, ParserConfigurationException {
        String repoURL = serviceConf.getProperty(CONFIG_AV_REPO_URL);
        if (repoURL == null || repoURL.isEmpty())
            throw new RemoteException("AV Repo access is not configured");

        HttpGet request = new HttpGet(repoURL + apiCommand + "/" + womiId);
        request.setHeader("Accept", "application/xml");
        HttpResponse response = getHttpClient().execute(request);
        int status = response.getStatusLine().getStatusCode();
        if (status != HttpStatus.SC_OK) {
            EntityUtils.consume(response.getEntity());
            throw new IOException("Unexpected AV Repository status: " + status);
        }
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(response.getEntity().getContent());
        EntityUtils.consume(response.getEntity());
        return document;
    }


    private boolean checkHead(String uri)
            throws IOException {
        HttpHead headRequest = new HttpHead(uri);
        HttpResponse response = getHttpClient().execute(headRequest);
        return response.getStatusLine().getStatusCode() == HttpStatus.SC_OK;
    }


    @Override
    public String getFolderStructure(String userLogin)
            throws RemoteException, DLibraException {
        UserServer us = DLStaticServiceResolver.getUserServer(getSr(), null);
        ActorId userId = us.getUserManager().getActorId(userLogin);
        DirectoryManager dm = DLStaticServiceResolver.getMetadataServer(getSr(), null).getDirectoryManager();
        RightManager rm = us.getRightManager();

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            Document doc = factory.newDocumentBuilder().newDocument();
            doc.setXmlStandalone(true);
            DirectoryInfo rootDir = (DirectoryInfo) dm
                    .getObjects(new DirectoryFilter(), new OutputFilter(DirectoryInfo.class)).getResultInfo();
            Element rootElement = createDirElement(doc, rootDir, userId, dm, rm);
            doc.appendChild(rootElement);

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(doc);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            transformer.transform(source, result);
            return writer.toString();
        } catch (TransformerException e) {
            throw new RemoteException("xml creation error", e);
        } catch (ParserConfigurationException e) {
            throw new RemoteException("xml creation error", e);
        } catch (TransformerFactoryConfigurationError e) {
            throw new RemoteException("xml creation error", e);
        }
    }


    private Element createDirElement(Document doc, DirectoryInfo dir, ActorId userId, DirectoryManager dm,
            RightManager rm)
                    throws RemoteException, DLibraException {
        Element subDirsElement = doc.createElement("directories");
        @SuppressWarnings({ "unchecked", "rawtypes" })
        Collection<DirectoryInfo> subdirs = (Collection) dm
                .getObjects(new DirectoryFilter(null, dir.getId()), new OutputFilter(DirectoryInfo.class))
                .getResultInfos();
        if (userId != null) {
            for (DirectoryInfo subdir : subdirs) {
                boolean hasRight = rm.checkDirectoryRight(subdir.getId(), userId, DirectoryRightId.PUBLICATION_CREATE);
                if (hasRight) {
                    subDirsElement.appendChild(createDirElement(doc, subdir, null, dm, rm));
                } else {
                    Element child = createDirElement(doc, subdir, userId, dm, rm);
                    if (child != null)
                        subDirsElement.appendChild(child);
                }
            }
            if (!subDirsElement.hasChildNodes())
                return null;
        } else {
            for (DirectoryInfo subdir : subdirs) {
                subDirsElement.appendChild(createDirElement(doc, subdir, null, dm, rm));
            }
        }
        Element dirElement = doc.createElement("directory");
        Element idElement = doc.createElement("id");
        idElement.setTextContent(dir.getId().toString());
        dirElement.appendChild(idElement);

        Element nameElement = doc.createElement("name");
        nameElement.setTextContent(dir.getLabel());
        dirElement.appendChild(nameElement);

        dirElement.appendChild(subDirsElement);
        return dirElement;
    }


    @Override
    public void reportAVMaterialUpdate(EditionId womiId)
            throws RemoteException, DLibraException {
        PublicationManager pm = DLStaticServiceResolver.getMetadataServer(getSr(), null).getPublicationManager();
        Edition womi = (Edition) pm.getObjects(new EditionFilter(womiId), new OutputFilter(Edition.class)).getResult();
        try {
            DefaultPasswordChecker.setUser(womi.getLastModifiedBy().getId());
            pm.setEditionData(womi); // forces update of modification date
            reportChange(womiId, RESOURCE_WOMI, COMMAND_MODIFIED);
        } finally {
            DefaultPasswordChecker.setUser(null);
        }
    }


    @Override
    public List<String> getAllModulesIds()
            throws RemoteException, DLibraException {
        List<String> result = null;
        try {
            HibernateController.beginTransaction();
            Session session = HibernateController.getSession();

            String query = "select edi.externalId from MetEdition edi where edi.externalId is not null "
                    + "and edi.state = " + Edition.PUBLISHED;
            Query q = session.createQuery(query);
            result = q.list();

            HibernateController.commitTransaction();
            HibernateController.closeSession();
        } catch (Exception e) {
            HibernateController.rollbackTransaction(e);
        }
        return result;
    }


    @Override
    public Map<Long, List<Long>> getAllTextbooksIds()
            throws RemoteException, DLibraException {
        Map<Long, List<Long>> result = null;
        try {
            HibernateController.beginTransaction();

            result = doGetAllTextbooksIds();

            HibernateController.commitTransaction();
            HibernateController.closeSession();
        } catch (Exception e) {
            HibernateController.rollbackTransaction(e);
        }
        return result;
    }


    private Map<Long, List<Long>> doGetAllTextbooksIds()
            throws RemoteException, DLibraException {
        Map<Long, List<Long>> result = new HashMap<Long, List<Long>>();
        Session session = HibernateController.getSession();

        MetadataServer ms = DLStaticServiceResolver.getMetadataServer(getSr(), null);
        AttributeId rootAttId = (AttributeId) ms.getAttributeManager()
                .getObjects(new AttributeFilter((AttributeId) null).setRDFNames(Arrays.asList("RootID")),
                    new OutputFilter(AttributeId.class))
                .getResultId();

        List<Long> oneVersion = Arrays.asList(1L);
        String query = "select pub.id from MetPublication pub where pub.groupStatus = " + Publication.PUB_GROUP_ROOT
                + "and pub.state = " + Publication.PUB_STATE_ACTUAL;
        List<Long> rootIds = session.createQuery(query).list();
        for (Long id : rootIds)
            result.put(id, oneVersion);

        query = "select pub.id from MetEdition edi, MetPublication pub "
                + "where edi.publication.id = pub.id and edi.externalId is null " + "and pub.state = "
                + Publication.PUB_STATE_ACTUAL + " and pub.groupStatus = " + Publication.PUB_NORMAL
                + " and edi.state = " + Edition.PUBLISHED + " and pub.mainFile.path <> '/"
                + WOMIXMLHandler.MAIN_FILE_NAME + "'";
        List<Long> separateTextbookIds = session.createQuery(query).list();
        for (Long id : separateTextbookIds)
            result.put(id, oneVersion);

        query = "select pa.comp_id.pubId, av.value from MetPubAtt pa, MetAttributeValue av " + "where pa.assocType = "
                + AttributeValue.AV_ASSOC_DIRECT + " and pa.comp_id.valueId = av.id and av.attribute.id = " + rootAttId;
        List<Object[]> tuples = session.createQuery(query).list();
        addTextbookVersions(tuples, result);

        query = "select edi.publication.id, av.value from MetEdiAtt ea, MetAttributeValue av, MetEdition edi "
                + "where ea.assocType = " + AttributeValue.AV_ASSOC_DIRECT + " and ea.comp_id.ediId = edi.id "
                + "and ea.comp_id.valueId = av.id and av.attribute.id = " + rootAttId;
        tuples = session.createQuery(query).list();
        addTextbookVersions(tuples, result);
        return result;
    }


    private void addTextbookVersions(List<Object[]> tuples, Map<Long, List<Long>> result) {
        for (Object[] tuple : tuples) {
            Long pubId = (Long) tuple[0];
            String rootValueString = (String) tuple[1];
            if (!result.containsKey(pubId)) {
                logger.warn("Object " + pubId + " has a RootID but is not a textbook");
                continue;
            }
            result.remove(pubId);
            Long rootValue;
            try {
                rootValue = Long.valueOf(rootValueString);
            } catch (NumberFormatException e) {
                logger.warn("Textbook " + pubId + " has invalid RootID: " + rootValueString);
                continue;
            }
            List<Long> versionsList = result.get(rootValue);
            if (versionsList == null) {
                logger.warn("Textbook " + pubId + " has a RootID " + rootValue + ", which is not a textbook");
                continue;
            }
            if (versionsList.size() == 1)
                result.put(rootValue, versionsList = new ArrayList<Long>(versionsList));
            versionsList.add((long) versionsList.size() + 1);
        }
    }
}
