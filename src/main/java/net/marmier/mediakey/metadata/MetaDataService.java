package net.marmier.mediakey.metadata;

import java.io.File;

/**
 * Added by raphael on 30.11.15.
 */
public interface MetaDataService {

    MetaData metadataFromFile(File file);

    boolean isSupportedFile(File file);
}
