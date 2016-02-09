package net.marmier.mediafilename.filename;

import net.marmier.mediafilename.metadata.MetaData;
import net.marmier.mediafilename.timezone.Offset;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Added by raphael on 27.12.15.
 */
public class FilenameGenerator {

    Offset tzOffset;

    public FilenameGenerator(Offset tzOffset) {
        this.tzOffset = tzOffset;
    }

    public String createUtcTimeZoneFilename(MetaData meta) {
        LocalDateTime utcDateTime = tzOffset.reverse(meta.getCaptureDateTime());


        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmmss");


        return String.format("%sutc_tz%s_%s", utcDateTime.format(formatter), tzOffset.toString(), meta.getFileName());
    }
}
