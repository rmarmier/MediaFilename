package net.marmier.mediakey.metadata;

import com.thebuzzmedia.exiftool.ExifTool;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Added by raphael on 13.12.15.
 */
public class PhotoMetaData implements MetaData {

    LocalDateTime captureDateTime;
    Map<ExifTool.Tag, String> valueMap;

    public PhotoMetaData(LocalDateTime captureDateTime) {
        this.captureDateTime = captureDateTime;
    }

    public String getFileName() {
        throw new UnsupportedOperationException();
    }

    public LocalDateTime getCaptureDateTime() {
        return captureDateTime;
    }
}
