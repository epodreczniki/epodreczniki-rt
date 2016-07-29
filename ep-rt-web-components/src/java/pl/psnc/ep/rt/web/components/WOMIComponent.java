package pl.psnc.ep.rt.web.components;

import java.rmi.RemoteException;
import java.util.Map;

import org.apache.velocity.context.Context;

import pl.psnc.dlibra.metadata.EditionId;
import pl.psnc.dlibra.metadata.Publication;
import pl.psnc.dlibra.mgmt.ServiceResolver;
import pl.psnc.dlibra.service.DLibraException;
import pl.psnc.dlibra.web.common.exceptions.LoginRequiredException;
import pl.psnc.dlibra.web.common.exceptions.PageGenerationException;
import pl.psnc.dlibra.web.common.pages.Module;
import pl.psnc.dlibra.web.common.util.RequestWrapper;
import pl.psnc.dlibra.web.comp.pages.components.AbstractDlibraComponent;
import pl.psnc.dlibra.web.comp.resources.ServicesManager;
import pl.psnc.dlibra.web.comp.utils.pages.ContentUrlBuilder;
import pl.psnc.dlibra.web.comp.utils.pages.DlibraRequestWrapper;
import pl.psnc.ep.rt.MediaFormat;
import pl.psnc.ep.rt.TargetFormat;
import pl.psnc.ep.rt.WOMIFormat;
import pl.psnc.ep.rt.WOMIType;
import pl.psnc.ep.rt.server.EPService;
import pl.psnc.ep.rt.server.EPService.AVInfo;
import pl.psnc.ep.rt.util.WOMIXMLHandler;
import pl.psnc.ep.rt.web.Util;

public class WOMIComponent extends AbstractDlibraComponent {

    @Override
    protected void prepareRemoteContent(RequestWrapper reqWr, Context context)
            throws RemoteException, DLibraException, PageGenerationException, LoginRequiredException {
        final DlibraRequestWrapper dReq = new DlibraRequestWrapper(reqWr);
        EditionId editionId = new EditionId(reqWr.getId());
        Publication pub = dReq.getPublication(editionId);
        ContentUrlBuilder cb = ContentUrlBuilder.getCachedContentUrlBuilder(pub, editionId, reqWr);
        if (!cb.getFile().getPath().equals("/" + WOMIXMLHandler.MAIN_FILE_NAME)) {
            return;
        }
        context.put("editionId", editionId);

        if (!context.get("pageId").equals("womi-selection"))
            return;
        WOMIType womiType;
        try {
            Map<WOMIFormat, String> filesMap = Util.loadWOMIData(editionId, null, null);

            womiType = filesMap.keySet().iterator().next().womiType;
            context.put("womiType", womiType);
            context.put("is3D",
                filesMap.containsKey(new WOMIFormat(womiType, MediaFormat.IMAGE, TargetFormat.CLASSIC, true)));
        } catch (Exception e) {
            throw new PageGenerationException("Could not parse main WOMI file (" + editionId + ")", e);
        }

        if (womiType == WOMIType.MOVIE || womiType == WOMIType.SOUND) {
            ServiceResolver sr = ServicesManager.getInstance().getServiceResolver();
            EPService ep = (EPService) sr.getService(EPService.SERVICE_TYPE, null);
            AVInfo avInfo = ep.getAVInfo(editionId);
            context.put("avInfo", avInfo);
            context.put("aspectRatio", ep.getVideoAspectRatio(editionId));
        }
    }


    @Override
    protected Module newModule() {
        return new WOMIComponent();
    }

}
