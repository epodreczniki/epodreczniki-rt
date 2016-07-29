package pl.psnc.ep.rt.ds;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;

import pl.psnc.dlibra.app.ApplicationContext;
import pl.psnc.dlibra.common.Info;
import pl.psnc.dlibra.common.OutputFilter;
import pl.psnc.dlibra.metadata.EditionFilter;
import pl.psnc.dlibra.metadata.EditionId;
import pl.psnc.dlibra.metadata.EditionInfo;
import pl.psnc.dlibra.metadata.ElementId;
import pl.psnc.dlibra.metadata.MetadataServer;
import pl.psnc.dlibra.metadata.attributes.AttributeFilter;
import pl.psnc.dlibra.metadata.attributes.AttributeId;
import pl.psnc.dlibra.metadata.attributes.RoleId;
import pl.psnc.ep.rt.WOMIType;

public class WOMIReferenceCreator {

    private static final String TEMPLATE_RESOURCE = "HtmlAttributesTable.html";

    private static final String PATTERN_WOMI_ID = "\\[WOMI_ID\\]";

    private static final String PATTERN_WOMI_TYPE = "\\[WOMI_TYPE\\]";

    private static final String PATTERN_TITLE = "\\[WOMI_TITLE\\]";

    private static final String PATTERN_NAME = "\\[WOMI_NAME\\]";


    private static class HtmlTransferable implements Transferable {

        private final DataFlavor supportedFlavor;

        private String content;


        public HtmlTransferable(String content) {
            this.content = content;
            try {
                supportedFlavor = new DataFlavor("text/html;class=java.lang.String");
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("There's no String class?", e);
            }
        }


        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return supportedFlavor.equals(flavor);
        }


        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[] { supportedFlavor };
        }


        @Override
        public Object getTransferData(DataFlavor flavor)
                throws UnsupportedFlavorException, IOException {
            if (!supportedFlavor.equals(flavor))
                throw new UnsupportedFlavorException(flavor);
            return content;
        }
    }


    public static void copyToClipboard(EditionId editionId, WOMIType womiType) {
        InputStream templateStream = WOMIReferenceCreator.class.getClassLoader().getResourceAsStream(TEMPLATE_RESOURCE);
        String template = new Scanner(templateStream, "UTF-8").useDelimiter("\\A").next();
        try {
            templateStream.close();
        } catch (IOException e) {
        }
        template = template.replaceAll(PATTERN_WOMI_ID, String.format("%05d", editionId.getId()));
        template = template.replaceAll(PATTERN_WOMI_TYPE, womiType.toExternalForm());
        template = template.replaceAll(PATTERN_TITLE, Matcher.quoteReplacement(getTitle(editionId)));
        template = template.replaceAll(PATTERN_NAME, Matcher.quoteReplacement(getName(editionId)));

        final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(new HtmlTransferable(template), null);
    }


    private static String getTitle(EditionId editionId) {
        try {
            MetadataServer ms = (MetadataServer) ApplicationContext.getInstance().getUserServiceResolver()
                    .getService(MetadataServer.SERVICE_TYPE, null);
            AttributeId titleAttributeId = (AttributeId) ms.getAttributeManager()
                    .getObjects(new AttributeFilter(RoleId.ROLE_TITLE), new OutputFilter(AttributeId.class))
                    .getResultId();
            Map<ElementId, List<String>> valuesMap = ms.getElementMetadataManager()
                    .getElementsAttributeValues(Arrays.asList((ElementId) editionId), titleAttributeId, "pl");
            List<String> values = valuesMap.get(editionId);
            if (values != null && !values.isEmpty())
                return values.get(0);
        } catch (Exception e) {
            throw new RuntimeException("Could not retrieve publication title", e);
        }
        return "";
    }


    private static String getName(EditionId editionId) {
        try {
            MetadataServer ms = (MetadataServer) ApplicationContext.getInstance().getUserServiceResolver()
                    .getService(MetadataServer.SERVICE_TYPE, null);
            Info info = ms.getPublicationManager()
                    .getObjects(new EditionFilter(editionId), new OutputFilter(EditionInfo.class)).getResultInfo();
            return info.getLabel();
        } catch (Exception e) {
            throw new RuntimeException("Could not retrieve publication name", e);
        }
    }
}
