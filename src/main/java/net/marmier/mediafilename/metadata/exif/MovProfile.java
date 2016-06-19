package net.marmier.mediafilename.metadata.exif;

import com.thebuzzmedia.exiftool.ExifTool;
import com.thebuzzmedia.exiftool.Tag;
import net.marmier.mediafilename.metadata.MetaData;
import net.marmier.mediafilename.metadata.PhotoMetaData;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Added by raphael on 13.05.2016.
 */
public class MovProfile extends AbstractExifProfile {

    /* "CreationDate" */
    private final ExtendedTags mediaCreateDate = ExtendedTags.MEDIA_CREATE_DATE;

    public MovProfile() {
        super(Pattern.compile(".+\\.(mov|MOV)$"));
    }

    public MetaData extractMeta(File file, ExifTool tool) throws IOException {
        List<Tag> tags = Arrays.asList(new ExtendedTags[] { mediaCreateDate });
        Map<Tag, String> valueMap = tool.getImageMeta(file, tags);
        String dateTime = valueMap.get(mediaCreateDate);
        return dateTime == null ?
            null :
            new PhotoMetaData(LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern("y:M:d H:m:s")), file.getName());
    }
}
