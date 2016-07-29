package pl.psnc.ep.rt.web.components;

import java.rmi.RemoteException;
import java.util.List;

import org.apache.velocity.context.Context;

import pl.psnc.dlibra.common.OutputFilter;
import pl.psnc.dlibra.metadata.AbstractPublicationInfo;
import pl.psnc.dlibra.metadata.Edition;
import pl.psnc.dlibra.metadata.MetadataServer;
import pl.psnc.dlibra.metadata.Publication;
import pl.psnc.dlibra.metadata.PublicationFilter;
import pl.psnc.dlibra.metadata.PublicationId;
import pl.psnc.dlibra.service.DLibraException;
import pl.psnc.dlibra.service.IdNotFoundException;
import pl.psnc.dlibra.web.common.exceptions.LoginRequiredException;
import pl.psnc.dlibra.web.common.exceptions.PageGenerationException;
import pl.psnc.dlibra.web.common.pages.Module;
import pl.psnc.dlibra.web.common.util.RequestWrapper;
import pl.psnc.dlibra.web.comp.pages.components.AbstractDlibraComponent;
import pl.psnc.dlibra.web.comp.resources.ServicesManager;
import pl.psnc.dlibra.web.comp.utils.pages.DlibraRequestWrapper;
import pl.psnc.ep.rt.util.Versioning;

public class EPRedirectComponent extends AbstractDlibraComponent {

    @Override
    protected void prepareRemoteContent(RequestWrapper reqWr, Context context)
            throws RemoteException, DLibraException, PageGenerationException, LoginRequiredException {
        PublicationId pubId = new DlibraRequestWrapper(reqWr).getPublicationId();

        MetadataServer ms = ServicesManager.getInstance().getMetadataServer();
        AbstractPublicationInfo publicationInfo = null;
        try {
            publicationInfo = (AbstractPublicationInfo) ms.getPublicationManager()
                    .getObjects(new PublicationFilter(pubId), new OutputFilter(AbstractPublicationInfo.class))
                    .getResultInfo();
        } catch (IdNotFoundException e) {
        }
        if (publicationInfo == null || publicationInfo.getState() != Publication.PUB_STATE_ACTUAL)
            return;

        @SuppressWarnings({ "unchecked", "rawtypes" })
        List<Edition> editions = (List) ms.getPublicationManager()
                .getObjects(new PublicationFilter(null, pubId), new OutputFilter(Edition.class)).getResults();
        if (editions.isEmpty() || editions.get(0).getExternalId() != null) {
            context.put("redirect", true);
            long[] contentIdAndVersion = Versioning.findContentIdAndVersion(pubId, ms);
            context.put("contentId", contentIdAndVersion[0]);
            context.put("version", contentIdAndVersion[1]);
        }
    }


    @Override
    protected Module newModule() {
        return new EPRedirectComponent();
    }

}
