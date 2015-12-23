package metadata;

import java.time.LocalDateTime;

/**
 * Created by raphael on 30.11.15.
 */
public interface MetaData {

    String getFileName();

    LocalDateTime getCaptureDateTime();
}
