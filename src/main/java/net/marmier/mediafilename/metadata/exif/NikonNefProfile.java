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
public class NikonNefProfile implements ExifProfile {

    public MetaData convert(File file, ExifTool tool) throws IOException {
        Map<Tag, String> valueMap = tool.getImageMeta(file, Collections.singletonList(StandardTag.DATE_TIME_ORIGINAL));
        String val = valueMap.get(StandardTag.DATE_TIME_ORIGINAL);
        return val == null ? null : new PhotoMetaData(LocalDateTime.parse(val, DateTimeFormatter.ofPattern("y:M:d H:m:s")), file.getName());
    }
}
