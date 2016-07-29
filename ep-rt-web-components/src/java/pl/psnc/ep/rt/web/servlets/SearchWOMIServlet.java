package pl.psnc.ep.rt.web.servlets;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import pl.psnc.dlibra.common.OutputFilter;
import pl.psnc.dlibra.metadata.AbstractPublicationInfo;
import pl.psnc.dlibra.metadata.DirectoryId;
import pl.psnc.dlibra.metadata.Edition;
import pl.psnc.dlibra.metadata.EditionFilter;
import pl.psnc.dlibra.metadata.EditionId;
import pl.psnc.dlibra.metadata.Language;
import pl.psnc.dlibra.metadata.LibCollectionId;
import pl.psnc.dlibra.metadata.MetadataServer;
import pl.psnc.dlibra.metadata.Publication;
import pl.psnc.dlibra.metadata.attributes.AbstractAttributeValue;
import pl.psnc.dlibra.metadata.attributes.AttributeId;
import pl.psnc.dlibra.metadata.attributes.AttributeValue;
import pl.psnc.dlibra.metadata.attributes.AttributeValueSet;
import pl.psnc.dlibra.search.AbstractSearchResult;
import pl.psnc.dlibra.search.LazySearchResultsHolder;
import pl.psnc.dlibra.search.LogicalOperator;
import pl.psnc.dlibra.search.QueryParseException;
import pl.psnc.dlibra.search.local.AdvancedQuery;
import pl.psnc.dlibra.search.local.LocalSearchResult;
import pl.psnc.dlibra.search.local.QueryElement;
import pl.psnc.dlibra.search.server.SearchServer;
import pl.psnc.dlibra.service.DLibraException;
import pl.psnc.dlibra.service.IdNotFoundException;
import pl.psnc.dlibra.web.common.exceptions.PublicIdentityProviderException;
import pl.psnc.dlibra.web.comp.resources.ServicesManager;
import pl.psnc.dlibra.web.fw.util.servlet.RequestWrapperFactory;
import pl.psnc.dlibra.web.fw.util.servlet.ServletRequestWrapper;
import pl.psnc.ep.rt.web.Util;

public class SearchWOMIServlet extends HttpServlet {

    private static final String ATTR_TITLE = "Tytul";

    private static final String ATTR_AUTHOR = "Autor";

    private static final String ATTR_KEYWORDS = "SlowaKluczowe";

    private static final String ATTR_IDENTIFIER = "IdentyfikatorWlasny";

    private static final String LANG = "pl";

    private static final String KEY_TITLE = "title";

    private static final String KEY_AUTHOR = "author";

    private static final String KEY_KEYWORDS = "keywords";

    private static final String KEY_IDENTIFIER = "identifier";

    private static final String KEY_MEDIA_TYPE = "media-type";

    private static final String KEY_COUNT = "count";

    private static final String KEY_ITEMS = "items";

    private static final String KEY_ID = "id";

    private static final String KEY_NAME = "name";

    private static final String[] SEARCH_TEXT_ATTRIBUTES = { ATTR_TITLE, "TekstAlternatywny", ATTR_AUTHOR,
            ATTR_IDENTIFIER };

    private static final LibCollectionId WOMI_COLLECTION = new LibCollectionId(19L);

    private static final Logger logger = Logger.getLogger(SearchWOMIServlet.class);


    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        ServletRequestWrapper reqWr;
        try {
            reqWr = RequestWrapperFactory.getInstance(req, resp);
        } catch (PublicIdentityProviderException e) {
            logger.error("Could not instantiate requestWrapper", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        String folderString = reqWr.getRequestParameter("folder");
        DirectoryId directory = null;
        try {
            directory = new DirectoryId(Long.valueOf(folderString));
        } catch (NumberFormatException e) {
        }
        EditionId womiId = null;
        String womiIdString = reqWr.getRequestParameter("womiId");
        try {
            womiId = new EditionId(Long.valueOf(womiIdString));
        } catch (NumberFormatException e) {
        }
        String all = reqWr.getRequestParameter("all");
        if ("".equals(all))
            all = null;
        String text = reqWr.getRequestParameter("text");
        if ("".equals(text))
            text = null;
        String keyword = reqWr.getRequestParameter("keyword");
        if ("".equals(keyword))
            keyword = null;
        String customId = reqWr.getRequestParameter("customId");
        if ("".equals(customId))
            customId = null;
        String pageIndexStr = reqWr.getRequestParameter("pageIndex");
        String pageSizeStr = reqWr.getRequestParameter("pageSize");

        if (womiId == null && all == null && directory == null && text == null && keyword == null && customId == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        if ((pageIndexStr == null) != (pageSizeStr == null)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        JSONObject result = null;
        try {
            MetadataServer ms = ServicesManager.getInstance().getMetadataServer();

            if (womiId != null) {
                result = findById(womiId, ms);
                return;
            }
            AdvancedQuery query = new AdvancedQuery();
            QueryElement qe = new QueryElement(Util.getAttributeId("Przeznaczenie", ms), "kzd embedded");
            qe.setOperation(LogicalOperator.NOT.getValue());
            query.addQueryElement(qe);

            if (all != null) {
                qe = new QueryElement(AttributeId.ALL_ATTRIBUTES_ATT_ID, all);
                qe.setOperation(LogicalOperator.OR.getValue());
                query.addQueryElement(qe);
            } else {
                if (text != null) {
                    for (String attribue : SEARCH_TEXT_ATTRIBUTES) {
                        qe = new QueryElement(Util.getAttributeId(attribue, ms), text);
                        qe.setOperation(LogicalOperator.OR.getValue());
                        query.addQueryElement(qe);
                    }
                }
                if (keyword != null) {
                    qe = new QueryElement(Util.getAttributeId(ATTR_KEYWORDS, ms), keyword);
                    qe.setOperation(LogicalOperator.OR.getValue());
                    query.addQueryElement(qe);
                }
                if (customId != null) {
                    qe = new QueryElement(Util.getAttributeId(ATTR_IDENTIFIER, ms), customId);
                    qe.setOperation(LogicalOperator.OR.getValue());
                    query.addQueryElement(qe);
                }
            }

            if (directory != null)
                query.setDirectoryId(directory);
            query.setState(Publication.PUB_NORMAL);
            query.setLibCollectionId(WOMI_COLLECTION);
            query.setLanguageName(ms.getLanguageManager().getLanguageNames(Language.LAN_DEFAULT_METADATA).get(0));
            query.setSortResults(true);

            SearchServer se = ServicesManager.getInstance().getSearchServer();

            int pageIndex = 0, pageSize = Integer.MAX_VALUE;
            if (pageIndexStr != null) {
                try {
                    pageIndex = Integer.valueOf(pageIndexStr);
                    pageSize = Integer.valueOf(pageSizeStr);
                } catch (NumberFormatException e) {
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }
            }
            if (pageSize <= 0 || pageIndex < 0) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            try {
                LazySearchResultsHolder<AbstractSearchResult> resultsHolder = se.getSearchManager().search(query,
                    pageIndex, pageSize);
                result = createResults(resultsHolder, pageIndex, ms);
            } catch (IndexOutOfBoundsException e) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
        } catch (DLibraException e) {
            logger.error("Error searching for WOMI", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        } catch (QueryParseException e) {
            logger.error("Error searching for WOMI", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        } finally {
            if (result != null) {
                byte[] content = result.toString(1).getBytes("UTF-8");
                resp.setContentLength(content.length);
                resp.setContentType(Util.JSON_CONTENT_TYPE);
                resp.getOutputStream().write(content);
            }
        }
    }


    private JSONObject findById(EditionId womiId, MetadataServer ms)
            throws RemoteException, DLibraException {
        JSONObject result = new JSONObject();
        JSONArray items = new JSONArray();
        result.put(KEY_ITEMS, items);

        Edition edition = (Edition) ms.getPublicationManager()
                .getObjects(new EditionFilter(womiId), new OutputFilter(Edition.class)).getResult();
        if (edition != null && edition.getExternalId() == null) {
            result.put(KEY_COUNT, 1);
            AttributeValueSet avs = ms.getElementMetadataManager().getAttributeValueSet(womiId,
                AttributeValue.AV_ASSOC_DIRECT);
            JSONObject item = new JSONObject();
            item.put(KEY_ID, womiId.getId().longValue());
            items.put(item);
            Collection<AbstractAttributeValue> titles = avs.getAttributeValues(Util.getAttributeId(ATTR_TITLE, ms));
            item.put(KEY_TITLE, (titles != null && !titles.isEmpty()) ? titles.iterator().next() : JSONObject.NULL);
            Collection<AbstractAttributeValue> authors = avs.getAttributeValues(Util.getAttributeId(ATTR_AUTHOR, ms));
            item.put(KEY_AUTHOR, toStringArray(authors));
            Collection<AbstractAttributeValue> keywords = avs
                    .getAttributeValues(Util.getAttributeId(ATTR_KEYWORDS, ms));
            item.put(KEY_KEYWORDS, toStringArray(keywords));
            Collection<AbstractAttributeValue> identifier = avs
                    .getAttributeValues(Util.getAttributeId(ATTR_IDENTIFIER, ms));
            item.put(KEY_IDENTIFIER, toStringArray(identifier));

            try {
                item.put(KEY_MEDIA_TYPE, MetadataServlet.getWOMIType(womiId, ms).toExternalForm());
            } catch (IOException e) {
                logger.error("Could not determine media type of womi " + womiId, e);
                item.put(KEY_MEDIA_TYPE, "!ERROR!");
            }
            addPublicationName(womiId, item, ms);
        } else {
            result.put(KEY_COUNT, 0);
        }
        return result;
    }


    private List<String> toStringArray(Collection<AbstractAttributeValue> values) {
        ArrayList<String> result = new ArrayList<String>();
        if (values == null)
            return result;
        for (AbstractAttributeValue value : values) {
            result.add(value.getValue());
        }
        return result;
    }


    private JSONObject createResults(LazySearchResultsHolder<AbstractSearchResult> resultsHolder, int pageIndex,
            MetadataServer ms)
                    throws RemoteException, DLibraException {
        JSONObject result = new JSONObject();
        result.put(KEY_COUNT, resultsHolder.getNormalHitsCount());
        JSONArray items = new JSONArray();
        result.put(KEY_ITEMS, items);

        @SuppressWarnings({ "unchecked", "rawtypes" })
        List<LocalSearchResult> searchResults = (List) resultsHolder.getResults(pageIndex);

        for (LocalSearchResult searchResult : searchResults) {
            EditionId womiId = (EditionId) searchResult.getEdition().getId();
            JSONObject item = new JSONObject();
            items.put(item);
            item.put(KEY_ID, womiId.getId().longValue());
            List<String> titles = searchResult.getAttributeValues(ATTR_TITLE, LANG);
            item.put(KEY_TITLE, (titles != null && !titles.isEmpty()) ? titles.get(0) : JSONObject.NULL);
            item.put(KEY_AUTHOR, searchResult.getAttributeValues(ATTR_AUTHOR, LANG));
            item.put(KEY_KEYWORDS, searchResult.getAttributeValues(ATTR_KEYWORDS, LANG));
            item.put(KEY_IDENTIFIER, searchResult.getAttributeValues(ATTR_IDENTIFIER, LANG));

            try {
                item.put(KEY_MEDIA_TYPE, MetadataServlet.getWOMIType(womiId, ms).toExternalForm());
            } catch (IOException e) {
                logger.error("Could not determine media type of womi " + womiId, e);
                item.put(KEY_MEDIA_TYPE, "!ERROR!");
            }
            addPublicationName(womiId, item, ms);
        }

        return result;
    }


    private void addPublicationName(EditionId womiId, JSONObject item, MetadataServer ms)
            throws IdNotFoundException, RemoteException, DLibraException {
        AbstractPublicationInfo pubInfo = (AbstractPublicationInfo) ms.getPublicationManager()
                .getObjects(new EditionFilter(womiId), new OutputFilter(AbstractPublicationInfo.class)).getResultInfo();
        if (pubInfo != null) {
            item.put(KEY_NAME, pubInfo.getLabel());
        } else {
            logger.error("Nonexisting womi in search results: " + womiId);
            item.put(KEY_NAME, "!ERROR!");
        }
    }
}
