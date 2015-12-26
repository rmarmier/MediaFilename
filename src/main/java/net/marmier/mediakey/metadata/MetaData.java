package net.marmier.mediakey.metadata;

import java.time.LocalDateTime;

/**
 * Added by raphael on 30.11.15.
 */
public interface MetaData {

    String getFileName();

    LocalDateTime getCaptureDateTime();
}
