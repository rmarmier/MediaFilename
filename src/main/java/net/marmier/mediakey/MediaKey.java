package net.marmier.mediakey;

import net.marmier.mediakey.metadata.MetaData;
import net.marmier.mediakey.metadata.MetaDataService;
import net.marmier.mediakey.metadata.exif.ExifProfile;
import net.marmier.mediakey.metadata.exif.ExiftoolMetaDataService;
import net.marmier.mediakey.metadata.exif.NikonNefProfile;
import net.marmier.mediakey.sig.SigGen;
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

    private static MediaKey mediaKey;

    private MetaDataService metaDataService = new ExiftoolMetaDataService();

    private SigGen sigGen;

    public String keyedNameForMedia(String filename) {
        LOG.info("Processing {}", filename);
        File file = new File(filename);

        MetaData meta = metaDataService.metadataFromFile(file);
        LOG.info(" --> Media creation datetime: {}", meta.getCaptureDateTime());
        String sig = sigGen.createSig(meta);
        LOG.info(" --> Resulting sig: {}", sig);
        return sig;
    }

    public MediaKey(String code) {
        sigGen = new UtcTimeZoneFilenameSig(Offset.forCode(code));
    }

    public static void main(String args[]) {
        if (args.length < 2) {
            System.out.println("Please provide path to a file.");
        } else {
            // Initialize the service
            String code = args[0];
            mediaKey = new MediaKey(code);

            final String filename = args[1];

            System.out.println(mediaKey.keyedNameForMedia(filename));
        }
    }
}
