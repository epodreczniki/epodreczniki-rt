package pl.psnc.ep.rt.server;

import static pl.psnc.dlibra.mgmt.DLStaticServiceResolver.getMetadataServer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;

import pl.psnc.dlibra.common.OutputFilter;
import pl.psnc.dlibra.metadata.AbstractPublicationInfo;
import pl.psnc.dlibra.metadata.MetadataServer;
import pl.psnc.dlibra.metadata.Publication;
import pl.psnc.dlibra.metadata.PublicationFilter;
import pl.psnc.dlibra.metadata.PublicationId;
import pl.psnc.dlibra.metadata.PublicationManager;
import pl.psnc.dlibra.metadata.attributes.AttributeFilter;
import pl.psnc.dlibra.metadata.attributes.AttributeId;
import pl.psnc.dlibra.metadata.attributes.AttributeManager;
import pl.psnc.dlibra.metadata.attributes.AttributeValueFilter;
import pl.psnc.dlibra.metadata.attributes.AttributeValueId;
import pl.psnc.dlibra.metadata.attributes.AttributeValueManager;
import pl.psnc.dlibra.mgmt.ServiceResolver;
import pl.psnc.dlibra.service.AbstractServiceFactory;
import pl.psnc.dlibra.user.User;
import pl.psnc.ep.rt.util.Versioning;
import pl.psnc.util.quartz.ObservableJob;

public class RemoveTemporaryTextbooksJob extends ObservableJob {

    private static final Logger logger = Logger.getLogger(RemoveTemporaryTextbooksJob.class);

    private static final String TEMP_ATTRIBUTE_RDF = "temp";

    private static final String TEMP_TRUE_VALUE = "true";

    private static final String LIFE_TIME_PARAM = "lifeTime";


    @Override
    protected void executeInternal(JobExecutionContext context) {
        try {
            JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
            Long lifeTime = jobDataMap.getLongFromString(LIFE_TIME_PARAM);
            if (lifeTime == null)
                lifeTime = 24L;
            lifeTime = lifeTime * 60 * 60 * 1000;

            ServiceResolver serviceResolver = (ServiceResolver) context.getScheduler().getContext()
                    .get(AbstractServiceFactory.SERVICE_RESOLVER_DATA_ENTRY);

            MetadataServer ms = getMetadataServer(serviceResolver, User.ADMIN_ID);
            AttributeManager am = ms.getAttributeManager();
            AttributeId attributeId = (AttributeId) am
                    .getObjects(new AttributeFilter((AttributeId) null).setRDFNames(Arrays.asList(TEMP_ATTRIBUTE_RDF)),
                        new OutputFilter(AttributeId.class))
                    .getResultId();

            AttributeValueManager avm = ms.getAttributeValueManager();
            AttributeValueId valueId = (AttributeValueId) avm
                    .getObjects(new AttributeValueFilter(attributeId).setValue(TEMP_TRUE_VALUE, true),
                        new OutputFilter(AttributeValueId.class))
                    .getResultId();

            @SuppressWarnings({ "unchecked", "rawtypes" })
            List<AbstractPublicationInfo> textbookInfos = (List) avm.getObjects(new AttributeValueFilter(null, valueId),
                new OutputFilter(AbstractPublicationInfo.class)).getResultInfos();
            List<PublicationId> textbookIds = new ArrayList<PublicationId>();
            for (AbstractPublicationInfo info : textbookInfos) {
                if (info.getStatus() == Publication.PUB_GROUP_ROOT)
                    textbookIds.add(info.getId());
            }

            logger.info("Checking " + textbookIds.size() + " temporary textbooks to remove: " + textbookIds);
            List<PublicationId> removedIds = new ArrayList<PublicationId>();
            PublicationManager pm = ms.getPublicationManager();
            @SuppressWarnings({ "unchecked", "rawtypes" })
            List<Publication> textbooks = (List) pm
                    .getObjects(new PublicationFilter(textbookIds), new OutputFilter(Publication.class)).getResults();
            long now = System.currentTimeMillis();
            for (Publication textbook : textbooks) {
                long modTime = textbook.getModificationTime().getTime();
                if (modTime + lifeTime < now) {
                    if (Versioning.hasNextVersion(textbook.getId(), ms)) {
                        logger.warn(
                            "Textbook " + textbook.getId() + " cannot be removed because it's not the last version");
                        continue;
                    }
                    pm.removePublication(textbook.getId(), true, "Temporary texbook");
                    removedIds.add(textbook.getId());
                }
            }
            logger.info("Removed " + removedIds.size() + " temporary textbooks: " + removedIds);
        } catch (Exception e) {
            logger.error("Error while removing temporary textbooks!", e);
        }
    }
}
