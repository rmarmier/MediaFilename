package net.marmier.mediafilename.metadata.exif;

import com.thebuzzmedia.exiftool.ExifTool;
import net.marmier.mediafilename.metadata.MetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Added by raphael on 07.05.16.
 */
public abstract class AbstractExifProfile implements ExifProfile {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    public AbstractExifProfile(Pattern filePattern) {
        this.filePattern = filePattern;
    }

    private final Pattern filePattern;

    @Override
    public MetaData extract(File file, ExifTool tool) throws IOException {
        String input = file.toString();
        if (filePattern.matcher(input).matches()) {
            log.debug("Extracting from file: {}", file.toString());
            return extractMeta(file, tool);
        }
        return null;
    }

    protected abstract MetaData extractMeta(File file, ExifTool tool) throws IOException;
}
