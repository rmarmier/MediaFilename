package metadata;

import metadata.exif.ExifProfile;

import java.io.File;

/**
 * Created by raphael on 30.11.15.
 */
public interface MetaDataService {

    MetaData metadataFromFile(File file, ExifProfile profile);
}
