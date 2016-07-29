package pl.psnc.ep.rt.tools.upload;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.rmi.RemoteException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import pl.psnc.dlibra.app.ApplicationContext;
import pl.psnc.dlibra.app.DLToolkit;
import pl.psnc.dlibra.app.common.exception.OperationFailedException;
import pl.psnc.dlibra.app.common.files.DataSourceEditionFile;
import pl.psnc.dlibra.app.common.files.EditionFile;
import pl.psnc.dlibra.app.parser.html.HtmlEncodingConverter;
import pl.psnc.dlibra.app.parser.html.ParseException;
import pl.psnc.dlibra.content.ContentServer;
import pl.psnc.dlibra.metadata.Version;
import pl.psnc.dlibra.metadata.VersionId;
import pl.psnc.dlibra.service.AccessDeniedException;
import pl.psnc.dlibra.service.DLibraException;
import pl.psnc.dlibra.service.IdNotFoundException;

public class FilesUploader {

    private final ContentServer contentServer;

    private List<VersionContent> files = new ArrayList<VersionContent>();

    private HtmlEncodingConverter htmlEncodingConverter;

    private Properties htmlConverterConfig;


    public FilesUploader(ContentServer contentServer) {
        this(contentServer, null);
    }


    public FilesUploader(ContentServer contentServer, Properties htmlConverterConfig) {
        this.contentServer = contentServer;
        this.htmlConverterConfig = htmlConverterConfig;
    }


    public void addFile(EditionFile content, VersionId versionId) {
        files.add(new VersionContent(content, versionId));
    }


    public void upload()
            throws DLibraException, IOException, ParseException, OperationFailedException {
        Map<VersionId, URL> urlsToUpload = new HashMap<VersionId, URL>();
        for (VersionContent aFile : files) {
            EditionFile ef = aFile.getContent();
            if (ef instanceof DataSourceEditionFile) {
                DataSourceEditionFile dsef = (DataSourceEditionFile) ef;
                if (!dsef.getFileInfo().isLocal()) {
                    urlsToUpload.put(aFile.getVerId(), dsef.getURL());
                    continue;
                }
            }
            uploadFile(ef, aFile.getVerId());
        }

        if (!urlsToUpload.isEmpty()) {
            Set<VersionId> failedVersions = contentServer.uploadVersions(urlsToUpload);
            if (!failedVersions.isEmpty()) {
                StringBuilder message = new StringBuilder("Resources could not be retrieved by the server: ");
                for (VersionId verId : failedVersions) {
                    message.append(urlsToUpload.get(verId)).append(", ");
                }
                message.setLength(message.length() - 2);
                throw new IOException(message.toString());
            }
        }
    }


    public void uploadFiles(List<EditionFile> filesToUpload, List<Version> createdVersions)
            throws RemoteException, DLibraException, IdNotFoundException, IOException, ParseException,
            OperationFailedException {
        if (filesToUpload.size() != createdVersions.size()) {
            throw new InvalidParameterException("Array length is not equal to List size!");
        }

        for (int j = 0; j < filesToUpload.size(); j++) {
            addFile(filesToUpload.get(j), createdVersions.get(j).getId());
        }

        upload();
    }


    private void uploadFile(EditionFile content, VersionId verId)
            throws IdNotFoundException, AccessDeniedException, DLibraException, IOException, ParseException,
            OperationFailedException {
        InputStream input = createInputStreamForFile(content);

        OutputStream versionOutputStream = contentServer.getVersionOutputStream(verId);

        try {
            byte[] buffer = new byte[DLToolkit.getIOBufferSize()];
            int bytesRead = 0;

            while ((bytesRead = input.read(buffer)) > 0)
                versionOutputStream.write(buffer, 0, bytesRead);
        } finally {
            input.close();
            versionOutputStream.close();
        }
    }


    @SuppressWarnings("resource")
    private InputStream createInputStreamForFile(EditionFile content)
            throws ParseException, IOException, UnsupportedEncodingException, OperationFailedException {
        InputStream is = null;

        if (content instanceof DataSourceEditionFile) {
            DataSourceEditionFile dsef = (DataSourceEditionFile) content;
            if (dsef.shouldDownloadLocally()) {
                is = dsef.getURL().openStream();
            }
        }
        if (is == null) {
            is = getEncodedInputStream(content);
        }

        return is;
    }


    public InputStream getEncodedInputStream(EditionFile publicationFile)
            throws ParseException, IOException, UnsupportedEncodingException, FileNotFoundException {
        InputStream inputStream = new FileInputStream(publicationFile.getSourceFile());

        if (publicationFile.isEncodable()) {
            ByteArrayOutputStream convertedBuffer = new ByteArrayOutputStream();
            getHTMLEncodingConverter().convert(inputStream, convertedBuffer);
            inputStream.close();
            inputStream = new ByteArrayInputStream(convertedBuffer.toByteArray());
        }
        return inputStream;
    }


    private HtmlEncodingConverter getHTMLEncodingConverter() {
        if (htmlEncodingConverter == null) {
            if (htmlConverterConfig == null) {
                htmlConverterConfig = ApplicationContext.getInstance().getGeneralConfiguration();
            }
            htmlEncodingConverter = new HtmlEncodingConverter(htmlConverterConfig);
        }
        return htmlEncodingConverter;
    }
}
