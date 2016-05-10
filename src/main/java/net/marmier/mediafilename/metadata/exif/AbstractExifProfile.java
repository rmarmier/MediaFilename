package net.marmier.mediafilename.metadata.exif;

import com.thebuzzmedia.exiftool.ExifTool;
import net.marmier.mediafilename.metadata.MetaData;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Added by raphael on 07.05.16.
 */
public abstract class AbstractExifProfile implements ExifProfile {

    public AbstractExifProfile(Pattern filePattern) {
        this.filePattern = filePattern;
    }

    private final Pattern filePattern;

    @Override
    public MetaData extract(File file, ExifTool tool) throws IOException {
        String input = file.toString();
        if (filePattern.matcher(input).matches()) {
            return extractMeta(file, tool);
        }
        return null;
    }

    protected abstract MetaData extractMeta(File file, ExifTool tool) throws IOException;
}
