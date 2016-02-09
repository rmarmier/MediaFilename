package net.marmier.mediafilename.metadata.exif;

import com.thebuzzmedia.exiftool.ExifTool;
import net.marmier.mediafilename.metadata.MetaData;

import java.io.File;
import java.io.IOException;

/**
 * Added by raphael on 11.12.15.
 */
public interface ExifProfile {

    MetaData convert(File file, ExifTool tool) throws IOException;

}
