package net.marmier.mediafilename.metadata.exif;

import com.thebuzzmedia.exiftool.ExifTool;
import net.marmier.mediafilename.metadata.MetaData;

import java.io.File;
import java.io.IOException;

/**
 * Added by raphael on 11.12.15.
 */
public interface ExifProfile {

    /**
     * Attempt to extract metadata from the passed file using
     * the passed Exiftool instance. By contract, return null if
     * the profile does not support the type of file.
     *
     * @param file the file to extract metadata from.
     * @param tool the Exiftool instance to be used.
     * @return the metadata object, or null if the file is not supported.
     * @throws IOException in case the file cannot be acessed.
     */
    MetaData extract(File file, ExifTool tool) throws IOException;

}
