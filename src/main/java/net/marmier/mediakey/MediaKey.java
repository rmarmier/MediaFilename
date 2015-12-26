package net.marmier.mediakey;

import net.marmier.mediakey.metadata.MetaDataService;
import net.marmier.mediakey.metadata.exif.ExifProfile;
import net.marmier.mediakey.metadata.exif.ExiftoolMetaDataService;
import net.marmier.mediakey.metadata.exif.NikonNefProfile;

import java.io.File;

/**
 * Added by raphael on 29.11.15.
 */
public class MediaKey {

    public static void main(String args[]){
        if (args.length < 1) {
            System.out.println("Please provide path to a file.");
        } else {
            final String filename = args[0];
            System.out.println(filename);
            File file = new File(filename);

            MetaDataService metaDataService = new ExiftoolMetaDataService();

            ExifProfile profile = new NikonNefProfile();

            metaDataService.metadataFromFile(file, profile);
        }
    }
}
