package pl.psnc.ep.rt.web.components;

import java.rmi.RemoteException;
import java.util.Properties;

import org.apache.velocity.context.Context;

import pl.psnc.dlibra.metadata.MetadataServer;
import pl.psnc.dlibra.metadata.PublicationId;
import pl.psnc.dlibra.service.DLibraException;
import pl.psnc.dlibra.web.common.exceptions.LoginRequiredException;
import pl.psnc.dlibra.web.common.exceptions.ModuleConfigurationException;
import pl.psnc.dlibra.web.common.exceptions.PageGenerationException;
import pl.psnc.dlibra.web.common.pages.Module;
import pl.psnc.dlibra.web.common.util.RequestWrapper;
import pl.psnc.dlibra.web.comp.pages.components.AbstractDlibraComponent;
import pl.psnc.dlibra.web.comp.resources.ServicesManager;
import pl.psnc.ep.rt.util.Versioning;

public class EPContentSelectionComponent extends AbstractDlibraComponent {

    private static final String CONFIG_PREVIEW_BASE_URL = "previewBaseUrl";

    private String previewBaseURL;


    @Override
    protected void prepareRemoteContent(RequestWrapper reqWr, Context context)
            throws RemoteException, DLibraException, PageGenerationException, LoginRequiredException {
        context.put("String", String.class);
        context.put("previewBaseUrl", previewBaseURL);

        PublicationId pubId = new PublicationId(reqWr.getId());

        MetadataServer ms = ServicesManager.getInstance().getMetadataServer();
        long[] contentIdAndVersion = Versioning.findContentIdAndVersion(pubId, ms);
        context.put("contentId", contentIdAndVersion[0]);
        context.put("version", contentIdAndVersion[1]);
    }


    @Override
    public void setConstantConfiguration(Properties props)
            throws ModuleConfigurationException {
        super.setConstantConfiguration(props);
        try {
            previewBaseURL = props.getProperty(CONFIG_PREVIEW_BASE_URL);
            if (previewBaseURL == null) {
                throw new ModuleConfigurationException("Component configuration required: " + CONFIG_PREVIEW_BASE_URL);
            }
        } catch (ModuleConfigurationException e) {
            throw e;
        } catch (Exception e) {
            throw new ModuleConfigurationException("unexpected error: " + e.getMessage());
        }
    }


    @Override
    protected Module newModule() {
        return new EPContentSelectionComponent();
    }

}
