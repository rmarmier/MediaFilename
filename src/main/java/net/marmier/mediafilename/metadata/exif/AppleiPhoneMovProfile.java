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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Added by raphael on 25.12.15.
 */
public class AppleiPhoneMovProfile extends AbstractExifProfile {

    /*
        MOV file created by apple's iPhone models have a "DateTimeOriginal" field set
        to UTC, when most (all?) other maker write a local datetime in that field. Fortunately,
        we can fall back to the "CreationDate" field which gives accurate datetime with a timezone.
     */

    private final String APPLE_MAKE = "Apple";
    private final String IPHONE_MODEL = "iPhone";

    /* "Make" */
    private final StandardTag makeField = StandardTag.MAKE;

    /* "Model" */
    private final StandardTag modelField = StandardTag.MODEL;

    /* "CreationDate" */
    private final StandardTag dateTimeField = StandardTag.CREATION_DATE;

    public AppleiPhoneMovProfile() {
        super(Pattern.compile(".+\\.(mov|MOV)$"));
    }

    public MetaData extractMeta(File file, ExifTool tool) throws IOException {
        List<Tag> tags = Arrays.asList(new StandardTag[] { dateTimeField, modelField, makeField });
        Map<Tag, String> valueMap = tool.getImageMeta(file, tags);
        String model = valueMap.get(modelField);
        String make = valueMap.get(makeField);
        if (APPLE_MAKE.equals(make) && model != null && model.startsWith(IPHONE_MODEL)) {
            String dateTime = valueMap.get(dateTimeField);
            return dateTime == null ?
                null :
                new PhotoMetaData(LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern("y:M:d H:m:sXXX")), file.getName());
        }
        return null;
    }
}
