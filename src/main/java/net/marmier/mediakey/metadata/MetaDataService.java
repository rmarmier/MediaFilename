package net.marmier.mediakey.metadata;

import net.marmier.mediakey.metadata.exif.ExifProfile;

import java.io.File;

/**
 * Added by raphael on 30.11.15.
 */
public interface MetaDataService {

    MetaData metadataFromFile(File file, ExifProfile profile);
}
