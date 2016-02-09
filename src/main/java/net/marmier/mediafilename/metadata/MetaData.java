package net.marmier.mediafilename.metadata;

import java.time.LocalDateTime;

/**
 * Added by raphael on 30.11.15.
 */
public interface MetaData {

    String getFileName();

    /**
     * @return the time the media was captured
     */
    LocalDateTime getCaptureDateTime();
}
