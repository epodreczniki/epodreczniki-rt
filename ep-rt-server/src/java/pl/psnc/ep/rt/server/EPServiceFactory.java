package pl.psnc.ep.rt.server;

import java.rmi.RemoteException;

import org.java.plugin.PluginManager;

import pl.psnc.dlibra.event.ElementEventMatcher;
import pl.psnc.dlibra.event.EventMatcher;
import pl.psnc.dlibra.event.metadata.CreateElementEvent;
import pl.psnc.dlibra.event.metadata.DeleteElementEvent;
import pl.psnc.dlibra.event.metadata.ModifyElementMetadataEvent;
import pl.psnc.dlibra.metadata.EditionId;
import pl.psnc.dlibra.metadata.PublicationId;
import pl.psnc.dlibra.mgmt.ServiceResolver;
import pl.psnc.dlibra.service.AbstractServiceFactory;
import pl.psnc.dlibra.service.DLibraException;
import pl.psnc.dlibra.service.ServiceUrl;
import pl.psnc.dlibra.service.conf.ServiceConfigurationBean;

public class EPServiceFactory extends AbstractServiceFactory<ServiceResolver> {

    public EPServiceFactory(ServiceUrl ssUrl, ServiceConfigurationBean serviceConf, PluginManager pluginManager)
            throws RemoteException, DLibraException {
        super(ssUrl, serviceConf, pluginManager);
    }


    @Override
    protected Class<?> getServiceClass() {
        return EPServiceImpl.class;
    }


    @Override
    protected EventMatcher getEventMatcher() {
        Class<?>[] eventClasses = new Class<?>[] { CreateElementEvent.class, DeleteElementEvent.class,
                ModifyElementMetadataEvent.class };
        Class<?>[] elementClasses = new Class<?>[] { EditionId.class, PublicationId.class };
        return new ElementEventMatcher(eventClasses, elementClasses);
    }

}
