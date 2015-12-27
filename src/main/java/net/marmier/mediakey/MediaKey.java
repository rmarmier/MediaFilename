package net.marmier.mediakey;

import net.marmier.mediakey.metadata.MetaData;
import net.marmier.mediakey.metadata.MetaDataService;
import net.marmier.mediakey.metadata.exif.ExifProfile;
import net.marmier.mediakey.metadata.exif.ExiftoolMetaDataService;
import net.marmier.mediakey.metadata.exif.NikonNefProfile;
import net.marmier.mediakey.sig.UtcTimeZoneFilenameSig;
import net.marmier.mediakey.tz.Offset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Added by raphael on 29.11.15.
 */
public class MediaKey {

    private static Logger LOG = LoggerFactory.getLogger(MediaKey.class);

    public static void main(String args[]) {
        if (args.length < 2) {
            System.out.println("Please provide path to a file.");
        } else {
            final String filename = args[1];
            LOG.info("Processing {}", filename);
            File file = new File(filename);

            MetaDataService metaDataService = new ExiftoolMetaDataService();

            ExifProfile profile = new NikonNefProfile();

            String code = args[0];
            UtcTimeZoneFilenameSig sigGen = new UtcTimeZoneFilenameSig(Offset.forCode(code));
            MetaData meta = metaDataService.metadataFromFile(file, profile);
            LOG.info(" --> Media creation datetime: {}", meta.getCaptureDateTime());
            LOG.info(" --> Resulting sig: {}", sigGen.createSig(meta));
        }
    }
}
