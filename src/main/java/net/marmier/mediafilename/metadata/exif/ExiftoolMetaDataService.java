package net.marmier.mediafilename.metadata.exif;

import com.thebuzzmedia.exiftool.ExifTool;
import com.thebuzzmedia.exiftool.ExifToolBuilder;
import net.marmier.mediafilename.metadata.MetaData;
import net.marmier.mediafilename.metadata.MetaDataService;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Added by raphael on 30.11.15.
 */
public class ExiftoolMetaDataService implements MetaDataService {

    private ExifTool tool;

    private List<ExifProfile> registeredProfiles = new ArrayList<>();

    public ExiftoolMetaDataService() {
        registeredProfiles.add(new JpgProfile());
        registeredProfiles.add(new NikonNefProfile());
        registeredProfiles.add(new AppleiPhoneMovProfile());
        registeredProfiles.add(new MovProfile());

        //tool = new ExifTool(ExifTool.Feature.STAY_OPEN);
    }

    @Override
    public MetaData metadataFromFile(File file) {

        try {
            for (ExifProfile profile : registeredProfiles) {
                MetaData metaData = profile.extract(file, getTool());
                if (metaData != null) {
                    return metaData;
                }
            }
        } catch (IOException e) {
            throw new ExiftoolMetaDataServiceException(String.format("File %s is unreadable.", file.getAbsoluteFile()), e);
        }
        return null;
    }

    public ExifTool getTool() {
        return new ExifToolBuilder().build();
    }
}
