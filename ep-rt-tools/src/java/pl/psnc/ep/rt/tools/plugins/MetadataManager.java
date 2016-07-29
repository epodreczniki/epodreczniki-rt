package pl.psnc.ep.rt.tools.plugins;

import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import pl.psnc.dlibra.app.DLToolkit;
import pl.psnc.dlibra.app.ExceptionHandler;
import pl.psnc.dlibra.app.common.metadata.RDFMetadataFinder;
import pl.psnc.dlibra.app.extension.Extension;
import pl.psnc.dlibra.app.extension.metadata.MetadataFinder;
import pl.psnc.dlibra.app.extension.metadata.MetadataFinder.FinderType;
import pl.psnc.dlibra.app.plugins.ApplicationExtension;
import pl.psnc.dlibra.metadata.MetadataServer;
import pl.psnc.dlibra.metadata.attributes.AttributeValueSet;
import pl.psnc.dlibra.service.DLibraException;
import pl.psnc.dlibra.service.IdNotFoundException;

public class MetadataManager {

    private List<MetadataFinder> finders = new ArrayList<MetadataFinder>();

    private static MetadataManager instance;


    public static MetadataManager getInstance() {
        if (instance == null) {
            try {
                instance = new MetadataManager();
            } catch (Exception e) {
                ExceptionHandler.getInstance().handleException(e, null);
            }
        }

        return instance;
    }


    private MetadataManager() {
        Collection<Extension> mf = ApplicationExtension.getInstance()
                .getExtensionsMap(ApplicationExtension.ExtensionType.METADATA).keySet();

        for (Iterator<Extension> iter = mf.iterator(); iter.hasNext();) {
            try {
                MetadataFinder element = (MetadataFinder) iter.next();
                registerMetadataFinder(element);
            } catch (ClassCastException e) {
                continue;
            }
        }

        RDFMetadataFinder internalMF = new RDFMetadataFinder();
        HashMap<String, Object> prefs = new HashMap<String, Object>();
        prefs.put(Extension.EXTENSION_DIRECTORY, DLToolkit.dLibraDataDirectory);
        internalMF.initialize(prefs);

        registerMetadataFinder(internalMF);
    }


    protected void registerMetadataFinder(MetadataFinder finder) {
        finders.add(finder);
    }


    public File findMetadataFile(File file) {
        for (MetadataFinder f : finders) {
            for (File metadataFile : makeMetadataFile(file, f)) {
                if (metadataFile.exists()) {
                    return metadataFile;
                }
            }
        }

        return null;
    }


    private List<File> makeMetadataFile(File file, MetadataFinder f) {

        String absPath = file.getAbsolutePath();
        int lastIndexOf = absPath.lastIndexOf(".");
        int lastIndexOfFS = absPath.lastIndexOf(File.separator);
        if (lastIndexOf != -1 && lastIndexOf > 1 && lastIndexOfFS < lastIndexOf) {
            absPath = absPath.substring(0, lastIndexOf);
        }

        List<File> files = new ArrayList<File>();
        for (String ext : f.getDefaultExtensions()) {
            String filePath = absPath + "." + ext;
            File metadataFile = new File(filePath);
            files.add(metadataFile);
        }

        return files;
    }


    public List<AttributeValueSet> findMetadata(File file, boolean ignoreExtension, MetadataServer ms)
            throws UnsupportedOperationException {
        List<File> potentialFiles = null;
        if (!ignoreExtension) {
            potentialFiles = new ArrayList<File>();
            potentialFiles.add(file);
        }
        for (MetadataFinder f : getRegisteredMetadataFinders(FinderType.IMPORT)) {
            if (ignoreExtension) {
                potentialFiles = makeMetadataFile(file, f);
            }
            for (File mFile : potentialFiles) {
                Logger.getLogger(getClass()).debug("Looking for metadata in " + mFile);
                List<AttributeValueSet> sets = null;
                try {
                    sets = MetadataConverter.convertToAVS(null, f.loadMetadata(null, mFile), "pl", ms);
                } catch (Throwable e) {
                    continue;
                }

                if (sets != null && sets.size() != 0) {
                    return sets;
                }
            }
        }

        return null;
    }


    public List<MetadataFinder> getRegisteredMetadataFinders(FinderType type) {
        List<MetadataFinder> findersList = new ArrayList<MetadataFinder>();
        for (MetadataFinder f : finders) {
            if (f.supportsType(type)) {
                findersList.add(f);
            }
        }
        return findersList;
    }


    public List<MetadataFinder> getRegisteredMetadataFinders() {
        List<MetadataFinder> findersList = new ArrayList<MetadataFinder>();
        for (MetadataFinder f : finders) {
            findersList.add(f);
        }

        return findersList;
    }


    public MetadataFinder getStoreRDFMetadataFinder() {
        for (MetadataFinder f : finders) {
            if (f instanceof RDFMetadataFinder) {
                if (f.supportsType(FinderType.EXPORT)) {
                    return f;
                }
            }
        }
        throw new RuntimeException("RDF metadata finder for storing RDF files not found");
    }


    public java.io.File createMetadataFile(File sourceFile, MetadataFinder finder) {
        List<File> files = makeMetadataFile(sourceFile, finder);
        if (!files.isEmpty()) {
            return files.get(0);
        }
        return null;
    }
}
