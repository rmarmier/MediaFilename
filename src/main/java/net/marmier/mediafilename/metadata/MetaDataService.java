package net.marmier.mediafilename.metadata;

import net.marmier.mediafilename.metadata.exif.ExiftoolMetaDataServiceException;

import java.io.File;

/**
 * Added by raphael on 30.11.15.
 */
public interface MetaDataService {

    MetaData metadataFromFile(File file) throws ExiftoolMetaDataServiceException;
}
