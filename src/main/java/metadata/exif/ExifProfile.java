package metadata.exif;

import com.thebuzzmedia.exiftool.ExifTool;
import metadata.MetaData;

import java.util.Map;

/**
 * Created by raphael on 11.12.15.
 */
public interface ExifProfile {

    MetaData convert(Map<ExifTool.Tag, String> valueMap);

}
