package pl.psnc.ep.rt.tools.upload;

import pl.psnc.dlibra.app.common.files.EditionFile;
import pl.psnc.dlibra.metadata.VersionId;

class VersionContent {

    private final EditionFile content;

    private final VersionId verId;


    public VersionContent(EditionFile content, VersionId verId) {
        this.content = content;
        this.verId = verId;
    }


    public EditionFile getContent() {
        return content;
    }


    public VersionId getVerId() {
        return verId;
    }


    public long getContentSize() {
        return content.getSourceFile().length();
    }
}