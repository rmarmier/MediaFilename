package net.marmier.mediafilename.metadata.exif;

import com.thebuzzmedia.exiftool.ExifTool;
import net.marmier.mediafilename.metadata.MetaData;
import net.marmier.mediafilename.metadata.MetaDataService;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Added by raphael on 30.11.15.
 */
public class ExiftoolMetaDataService implements MetaDataService {

    private ExifTool tool;

    private ExifProfile jpgProfile;
    private ExifProfile nikonNefProfile;

    Pattern jpgPattern = Pattern.compile(".+\\.(jpg|JPG|jpeg|JPEG)$");
    Pattern nikonNefPattern = Pattern.compile(".+\\.(nef|NEF)$");


    public ExiftoolMetaDataService() {
        jpgProfile = new JpgProfile();
        nikonNefProfile = new NikonNefProfile();

        //tool = new ExifTool(ExifTool.Feature.STAY_OPEN);
    }

    @Override
    public MetaData metadataFromFile(File file) {

        ExifProfile profileToUse = determineProfile(file);
        try {
            return profileToUse.convert(file, getTool());
        } catch (IOException e) {
            throw new ExiftoolMetaDataServiceException(String.format("File %s is unreadable.", file.getAbsoluteFile()), e);
        }
    }

    @Override
    public boolean isSupportedFile(File file) {
        return getProfile(file) != null;
    }

    public ExifProfile determineProfile(File file) {
        ExifProfile profile = getProfile(file);
        if (profile == null) {
            throw new IllegalArgumentException("Unrecognized file format: " + file.toString());
        }
        return profile;
    }

    private ExifProfile getProfile(File file) {
        if (jpgPattern.matcher(file.toString()).matches()) {
            return jpgProfile;
        }
        else if (nikonNefPattern.matcher(file.toString()).matches()) {
            return nikonNefProfile;
        }
        return null;
    }

    public ExifTool getTool() {
        return new ExifTool();
    }
}
