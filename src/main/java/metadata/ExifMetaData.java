package metadata;

import com.thebuzzmedia.exiftool.ExifTool;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Created by raphael on 13.12.15.
 */
public class ExifMetaData implements MetaData {

    Map<ExifTool.Tag, String> valueMap;

    public ExifMetaData(Map<ExifTool.Tag, String> valueMap) {
        this.valueMap = valueMap;
    }

    public String getFileName() {
        throw new UnsupportedOperationException();
    }

    public LocalDateTime getCaptureDateTime() {
        String val = valueMap.get(ExifTool.Tag.DATE_TIME_ORIGINAL);
        return val == null ? null : LocalDateTime.parse(val, DateTimeFormatter.ofPattern("y:M:d H:m:s"));
    }
}
