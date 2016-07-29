package pl.psnc.ep.rt.server;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

import pl.psnc.dlibra.metadata.EditionId;
import pl.psnc.dlibra.metadata.PublicationId;
import pl.psnc.dlibra.service.DLibraException;
import pl.psnc.dlibra.service.Service;
import pl.psnc.dlibra.service.ServiceType;

public interface EPService extends Service {

    public static final ServiceType SERVICE_TYPE = new ServiceType("ep");


    public static class AVInfo implements Serializable {

        public boolean isVideo;

        public boolean hasAudiodescription;

        public boolean hasCaptions;

        public boolean hasSubtitles;
    }


    public void reportTextbookCreated(PublicationId publicationId)
            throws RemoteException;


    public void textbookModificationStart(PublicationId publicationId)
            throws RemoteException;


    public void textbookModificationEnd(PublicationId publicationId, boolean created)
            throws RemoteException;


    public void reportTextbookAccepted(PublicationId publicationId)
            throws RemoteException;


    public void reportTextbookRemoved(PublicationId publicationId)
            throws RemoteException;


    public void reportWOMIModified(EditionId womiId)
            throws RemoteException, DLibraException;


    public AVInfo getAVInfo(EditionId womiId)
            throws RemoteException, DLibraException;


    public Double getVideoAspectRatio(EditionId womiId)
            throws RemoteException, DLibraException;


    public String getFolderStructure(String userLogin)
            throws RemoteException, DLibraException;


    public void reportAVMaterialUpdate(EditionId womiId)
            throws RemoteException, DLibraException;


    public List<String> getAllModulesIds()
            throws RemoteException, DLibraException;


    public Map<Long, List<Long>> getAllTextbooksIds()
            throws RemoteException, DLibraException;
}
