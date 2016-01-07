package net.marmier.mediakey;

import net.marmier.mediakey.metadata.MetaData;
import net.marmier.mediakey.metadata.MetaDataService;
import net.marmier.mediakey.metadata.exif.ExiftoolMetaDataService;
import net.marmier.mediakey.sig.SignatureGenerator;
import net.marmier.mediakey.tz.Offset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Added by raphael on 29.11.15.
 */
public class MediaKey {

    private static Logger LOG = LoggerFactory.getLogger(MediaKey.class);

    private static MediaKey mediaKey;

    private MetaDataService metaDataService = new ExiftoolMetaDataService();

    private SignatureGenerator sigGen;

    public String keyedNameForMedia(File file) {

        MetaData meta = metaDataService.metadataFromFile(file);
        LOG.info(" --> Media creation datetime: {}", meta.getCaptureDateTime());
        String sig = sigGen.createUtcTimeZoneFilenameSig(meta);
        LOG.info(" --> Resulting sig: {}", sig);
        return sig;
    }

    public MediaKey(Offset code) {
        sigGen = new SignatureGenerator(code);
    }

    public static void main(String args[]) throws IOException {
        if (args.length < 2) {
            showUsage(null);
        } else {
            // Initialize the service
            String code = args[0];
            Offset decoded = null;
            try {
                decoded = Offset.forCode(code);
            } catch (IllegalArgumentException e) {
                showUsage("Invalid timezone code provided: " + code);
                System.exit(1);
            }
            mediaKey = new MediaKey(decoded);

            final String filename = args[1];
            File file = checkAndGetFile(filename);

            if (file.isDirectory()) {
                processDirectory(file.toPath());
            } else {
                LOG.info("Processing {}", filename);
                System.out.println(mediaKey.keyedNameForMedia(file));
            }
        }
    }

    private static File checkAndGetFile(String filename) {
        return new File(filename);
    }

    private static void showUsage(String additionalInfo) {
        String usage = "Please provide a valid timezone (ex. +01:00) and the path to the directory containing the media to rename.";
        System.out.println(usage);
        if (additionalInfo != null) {
            System.out.println(additionalInfo);
        }
    }

    private static void processDirectory(Path directory) throws IOException {
        Files.walkFileTree(directory, new FileVisitor<Path>() {
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                LOG.info("Processing {}", file.getFileName());
                System.out.println(mediaKey.keyedNameForMedia(file.toFile()));
                return FileVisitResult.CONTINUE;
            }

            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                exc.printStackTrace();
                return FileVisitResult.CONTINUE;
            }

            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
