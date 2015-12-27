package net.marmier.mediakey.metadata.exif;

import com.thebuzzmedia.exiftool.ExifTool;
import net.marmier.mediakey.metadata.MetaData;
import net.marmier.mediakey.metadata.PhotoMetaData;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Added by raphael on 25.12.15.
 */
public class NikonNefProfile implements ExifProfile {

    public MetaData convert(File file, ExifTool tool) throws IOException {
        Map<ExifTool.Tag, String> valueMap = tool.getImageMeta(file, ExifTool.Tag.DATE_TIME_ORIGINAL);
        String val = valueMap.get(ExifTool.Tag.DATE_TIME_ORIGINAL);
        return val == null ? null : new PhotoMetaData(LocalDateTime.parse(val, DateTimeFormatter.ofPattern("y:M:d H:m:s")), file.getName());
    }
}
