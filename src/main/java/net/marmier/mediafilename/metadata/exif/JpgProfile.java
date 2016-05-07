package net.marmier.mediafilename.metadata.exif;

import com.thebuzzmedia.exiftool.ExifTool;
import com.thebuzzmedia.exiftool.Tag;
import com.thebuzzmedia.exiftool.core.StandardTag;
import net.marmier.mediafilename.metadata.MetaData;
import net.marmier.mediafilename.metadata.PhotoMetaData;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;

/**
 * Added by raphael on 25.12.15.
 */
public class JpgProfile implements ExifProfile {

    /* "DateTimeOriginal" */
    private final StandardTag dateTimeField = StandardTag.DATE_TIME_ORIGINAL;

    public MetaData convert(File file, ExifTool tool) throws IOException {
        Map<Tag, String> valueMap = tool.getImageMeta(file, Collections.singletonList(dateTimeField));
        String dateTime = valueMap.get(dateTimeField);
        return dateTime == null ? null : new PhotoMetaData(LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern("y:M:d H:m:s")), file.getName());
    }
}
