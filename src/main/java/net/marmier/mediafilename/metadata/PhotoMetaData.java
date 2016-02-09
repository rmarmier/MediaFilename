package net.marmier.mediafilename.metadata;

import java.time.LocalDateTime;

/**
 * Added by raphael on 13.12.15.
 */
public class PhotoMetaData implements MetaData {

    String filename;

    LocalDateTime captureDateTime;

    public PhotoMetaData(LocalDateTime captureDateTime, String filename) {
        this.filename = filename;
        this.captureDateTime = captureDateTime;
    }

    public String getFileName() {
        return filename;
    }

    public LocalDateTime getCaptureDateTime() {
        return captureDateTime;
    }
}
