package metadata.exif;

import com.thebuzzmedia.exiftool.ExifTool;
import metadata.MetaData;
import metadata.MetaDataService;

import java.io.File;
import java.io.IOException;

/**
 * Created by raphael on 30.11.15.
 */
public class ExiftoolMetaDataService implements MetaDataService {

    private ExifTool tool;

    public ExiftoolMetaDataService() {
        //tool = new ExifTool(ExifTool.Feature.STAY_OPEN);
    }

    public MetaData metadataFromFile(File file, ExifProfile profile) {
        try {
            return profile.convert(getTool().getImageMeta(file, ExifTool.Tag.DATE_TIME_ORIGINAL));
        } catch (IOException e) {
            throw new ExiftoolMetaDataServiceException(String.format("File %s is unreadable.", file.getAbsoluteFile()), e);
        }
    }

    public ExifTool getTool() {
        return new ExifTool();
    }
}
