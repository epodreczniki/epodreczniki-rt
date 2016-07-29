package pl.psnc.ep.rt.listeners;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.FocusManager;
import javax.swing.JOptionPane;

import pl.psnc.dlibra.common.OutputFilter;
import pl.psnc.dlibra.metadata.AbstractPublicationInfo;
import pl.psnc.dlibra.metadata.ElementId;
import pl.psnc.dlibra.metadata.MetadataServer;
import pl.psnc.dlibra.metadata.Publication;
import pl.psnc.dlibra.metadata.PublicationFilter;
import pl.psnc.dlibra.metadata.PublicationId;
import pl.psnc.dlibra.metadata.attributes.AbstractAttributeValue;
import pl.psnc.dlibra.metadata.attributes.AttributeValue;
import pl.psnc.dlibra.metadata.attributes.AttributeValueSet;
import pl.psnc.dlibra.metadata.attributes.AttributeValueSet.Values;
import pl.psnc.dlibra.metadata.attributes.ElementMetadataManager;

public class PublicationDateEventListener extends AbstractMetadataListener {

    private static final String PUBLICATION_DATE_ATTR_RDFNAME = "DataPublikacji";


    @Override
    protected void validateObservedValue(ElementId elementId, MetadataServer ms, ElementMetadataManager emm)
            throws Exception {
        if (observedAttribute == null && !init(PUBLICATION_DATE_ATTR_RDFNAME))
            return;

        AttributeValueSet avs = emm.getAttributeValueSet(elementId, AttributeValue.AV_ASSOC_DIRECT);
        List<AbstractAttributeValue> publicationDateAVs = avs.getAttributeValues(observedAttribute, defaultLanguage,
            Values.OnlyDirect);
        if (publicationDateAVs == null || publicationDateAVs.isEmpty())
            return;

        boolean isBook = (elementId instanceof PublicationId);
        if (isBook) {
            AbstractPublicationInfo pubInfo = (AbstractPublicationInfo) ms.getPublicationManager()
                    .getObjects(new PublicationFilter((PublicationId) elementId),
                        new OutputFilter(AbstractPublicationInfo.class))
                    .getResultInfo();
            isBook = pubInfo.getStatus() == Publication.PUB_GROUP_ROOT;
        }
        if (!isBook) {
            JOptionPane.showMessageDialog(FocusManager.getCurrentManager().getActiveWindow(),
                String.format(textsBundle.getString("publicationDate.error.notABook"), publicationDateAVs.get(0)),
                textsBundle.getString("publicationDate.error.title"), JOptionPane.ERROR_MESSAGE);
            removeObservedValues(false, elementId, emm);
            return;
        }

        if (!checkPublicationDateId(publicationDateAVs.get(0).getValue())) {
            JOptionPane.showMessageDialog(FocusManager.getCurrentManager().getActiveWindow(),
                String.format(textsBundle.getString("publicationDate.error.invalid"), publicationDateAVs.get(0)),
                textsBundle.getString("publicationDate.error.title"), JOptionPane.ERROR_MESSAGE);
            removeObservedValues(false, elementId, emm);
            return;
        }

        if (publicationDateAVs.size() > 1) {
            JOptionPane.showMessageDialog(FocusManager.getCurrentManager().getActiveWindow(),
                textsBundle.getString("publicationDate.warning.tooMany"),
                textsBundle.getString("publicationDate.warning.title"), JOptionPane.WARNING_MESSAGE);
            removeObservedValues(true, elementId, emm);
        }
    }


    private boolean checkPublicationDateId(String value) {
        Matcher matcher = Pattern.compile("([0-9]{4})-([0-9]{2})-([0-9]{2})").matcher(value);
        if (!matcher.matches())
            return false;
        try {
            int year = Integer.parseInt(matcher.group(1));
            int month = Integer.parseInt(matcher.group(2));
            int dayOfMonth = Integer.parseInt(matcher.group(3));
            Date date = new SimpleDateFormat("yyyy-MM-dd").parse(value);
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTime(date);
            if (calendar.get(Calendar.YEAR) != year)
                return false;
            if (calendar.get(Calendar.MONTH) + 1 != month)
                return false;
            if (calendar.get(Calendar.DAY_OF_MONTH) != dayOfMonth)
                return false;
        } catch (NumberFormatException e) {
            return false;
        } catch (ParseException e) {
            return false;
        }
        return true;
    }

}
