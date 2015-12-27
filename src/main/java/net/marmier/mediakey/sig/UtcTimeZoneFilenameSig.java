package net.marmier.mediakey.sig;

import net.marmier.mediakey.metadata.MetaData;
import net.marmier.mediakey.tz.Offset;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Added by raphael on 27.12.15.
 */
public class UtcTimeZoneFilenameSig implements SigGen {

    Offset tzOffset;

    public UtcTimeZoneFilenameSig(Offset tzOffset) {
        this.tzOffset = tzOffset;
    }

    public String createSig(MetaData meta) {
        LocalDateTime utcDateTime = tzOffset.reverse(meta.getCaptureDateTime());


        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmmss");


        return String.format("%sutc_tz%s_%s", utcDateTime.format(formatter), tzOffset.toString(), meta.getFileName());
    }
}
