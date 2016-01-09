package net.marmier.mediakey;

import net.marmier.mediakey.metadata.MetaData;
import net.marmier.mediakey.metadata.MetaDataService;
import net.marmier.mediakey.metadata.exif.ExiftoolMetaDataService;
import net.marmier.mediakey.sig.SignatureGenerator;
import net.marmier.mediakey.tz.Offset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <h4>Purpose</h4>
 * <p>
 *     To generate new filenames for media files, so files from different sources can be archived together
 *     with a consistent ordering.
 * </p>
 *
 * <h4>Arguments</h4>
 * <p>
 *     1) A timezone definition, from those defined in the Offset class. For historical reasons and compatibility
 *     with an older tool.
 * </p>
 * <p>
 *     2) A file or directory containing targeted media files.
 * </p>
 *
 * <h4>Behaviour</h4>
 * <p>
 *     The tool first determines the working directory. The parent directory of the targeted file or directory
 *     will be used, unless the given path is relative, in which case the user.dir system property will be assumed
 *     to be the desired working directory.
 * </p>
 * <p>
 *     If a directory has been given, it is traversed recursively is search for supported media files.
 * </p>
 * <p>
 *     The tool generates a rename command for each supported media files. It embeds the UTC time and the
 *     timezone into the new filename so the usual alphanumerical sort will keep files in chronological order.
 * </p>

 * <h4>Format of the generated filenames</h4>
 * <p>
 *     The new name for the media file implement the following format:
 * </p>
 * <p>
 *     <strong>[Year]-[Month]-[Day]_[hours][minutes][seconds]utc_tz[timezone]_[original file name]</strong>
 * </p>
 *     Example:<br>
 *     2015-12-03_074904utc_tz+0100_DSC_5926.JPG
 *<p>
 *     This format allows the natural ordering of media files from various origins according to
 *     the time of capture, independently of the timezone.
 *</p>
 * <h4>Supported media files</h4>
 *
 * <table>
 * <tr>
 *     <th>File type</th><th>EXIF field</th><th>Explanation</th>
 * </tr>
 * <tr>
 *     <td>JPEG</td><td>DateTimeOriginal</td><td>Generic support for JPEG files, indifferent of camera make.</td>
 * </tr>
 * <tr>
 *     <td>Nikon NEF</td><td>DateTimeOriginal</td><td>Support for Nikon NEF raw format pictures.</td>
 * </tr>
 * </table>
 *
 * <h4>How to use the result</h4>
 * <p>
 *     The tool sends the rename command simultaneously to the standard output and to a shell command file in the
 *     working directory. The command file can be executed or sourced in place.
 * </p><p>
 *     Additionally, it produces two log files, one detailing the processing and one for errors only (for historical
 *     and compatibility reasons with an earlier GUI wrapper).
 * </p>
 *
 * <h4>System requirements</h4>
 * <ul>
 *     <li>Java 8</li>
 * </ul>
 *
 * <h4>Version history</h4>
 * v 1.0 - 2016-02-09 - Initial usable version.
 *
 * Added by raphael on 29.11.15.
 */
public class MediaKey {

    private static MediaKey mediaKey;

    private Logger log = LoggerFactory.getLogger(MediaKey.class);

    private MetaDataService metaDataService = new ExiftoolMetaDataService();

    private SignatureGenerator sigGen;

    private final Path workingDirectory;

    public MediaKey(Offset code, String workingDirectory) {
        sigGen = new SignatureGenerator(code);
        this.workingDirectory = new File(workingDirectory).toPath();
    }

    public static void main(String args[]) throws IOException {
        LocalDateTime launchTime = LocalDateTime.now();

        if (args.length < 2) {
            showUsage(null);
        } else {

            // Decode the timezone offset
            Offset offset = decodeCode(args[0]);

            // Get the fileArgumentName parameter
            File targetFile = new File(args[1]);

            // Determine the working directory. We fall back on the system property if that fails.
            String workingDirectory = getWorkingDirectory(targetFile);

            System.err.println("targetFile: " + targetFile);
            System.err.println("workingDirectory: " + workingDirectory);

            Path commandFile;

            // Assemble the output files basename
            String filenameBase = String.format(
                "Importation_%s_%s_tz%s",
                launchTime.format(DateTimeFormatter.ISO_LOCAL_DATE),
                targetFile.getName(), offset.toString()
            );
            System.err.println("filenameBase: " + filenameBase);

            // Prepare the command targetFile name
            commandFile = new File(workingDirectory + "/" + filenameBase + ".sh").toPath();

            // Initialize the log fileArgumentName dynamically. This work provided we use non-static loggers.
            System.setProperty("logfilename", workingDirectory + "/" + filenameBase);

            // Initialize the error log fileArgumentName dynamically. This work provided we use non-static loggers.
            System.setProperty("errorsfilename", workingDirectory + "/" + filenameBase + "_errors");

            // Initialize the service (last, so we can configure the log targetFile dynamically, just above)
            mediaKey = new MediaKey(offset, workingDirectory);

            // Process the media files and get the results
            final List<Result> results = mediaKey.process(targetFile);

            // Generate the command targetFile
            writeResult(commandFile, results);
        }
    }

    /**
     * We pick the parent directory of the targeted file or directory as the working directory. Should that be
     * impossible (because the path given is relative), we get the user.dir property from the system.
     * @param targetFile the file or directory targeted by the program
     * @return the string representing the working directory
     */
    private static String getWorkingDirectory(File targetFile) {
        String workingDirectory = targetFile.getParent();
        if (workingDirectory == null) {
            workingDirectory = System.getProperty("user.dir");
            System.err.println("Using user.dir as working dir.");
        }
        return workingDirectory;
    }

    private static Offset decodeCode(String code) {
        Offset offset = null;
        try {
            offset = Offset.forCode(code);
        } catch (IllegalArgumentException e) {
            showUsage("Invalid timezone code provided: " + code);
            System.exit(1);
        }
        return offset;
    }

    private static void writeResult(Path commandFile, List<Result> results) throws IOException {
        backupIfExist(commandFile);
        try (BufferedWriter writer = Files.newBufferedWriter(commandFile, StandardOpenOption.CREATE_NEW)) {
            for (Result result : results) {
                String command = String.format("mv \'%s\' \'%s\'", result.before, result.after);
                System.out.println(command);
                writer.write(command);
                writer.write("\n");
            }
        } catch (Exception e) {
            // Last ditch
            e.printStackTrace();
        }
    }

    private static void backupIfExist(Path commandFile) throws IOException {
        if (Files.exists(commandFile)) {
            Files.move(commandFile, createBackupFilename(commandFile));
        }
    }

    private static Path createBackupFilename(Path commandFile) {
        return new File(commandFile.toString() + "-saved-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmmss")) + ".sh").toPath();
    }

    private static void showUsage(String additionalInfo) {
        String usage = "Please provide a valid timezone (ex. +01:00) and the path to the directory containing the media to rename.";
        System.err.println(usage);
        if (additionalInfo != null) {
            System.err.println(additionalInfo);
        }
    }

    private List<Result> process(File file) throws IOException {
        if (file.isDirectory()) {
            return processDirectory(file.toPath());
        } else {
            Result result = processFile(file.toPath());
            if (result != null) {
                return Collections.singletonList(result);
            }
            else {
                return Collections.emptyList();
            }
        }
    }

    private List<Result> processDirectory(Path targetDirectory) throws IOException {
        final List<Result> results = new ArrayList<>();
        Files.walkFileTree(targetDirectory, new FileVisitor<Path>() {
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Result result = processFile(file);
                if (result != null) {
                    results.add(result);
                }
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
        return results;
    }

    private Result processFile(Path originalFile) {
        log.info("Processing {}", originalFile.getFileName());

        String oldRelativeName = createOldRelativePath(originalFile);

        if (metaDataService.isSupportedFile(originalFile.toFile())) {
            String newName = mediaKey.generateFilename(originalFile.toFile());

            String newRelativeName = createNewRelativePath(originalFile, newName);

            return new Result(oldRelativeName, newRelativeName);
        }

        log.info("File has unrecognized extension: ignored ({})", oldRelativeName);
        return null;
    }

    private String createNewRelativePath(Path originalFile, String newName) {
        Path parentDir = originalFile.getParent();
        String newRelativeParent = workingDirectory.relativize(parentDir.toAbsolutePath()).toString();
        return String.format("%s%s", newRelativeParent + "/", newName);
    }

    private String createOldRelativePath(Path originalFile) {
        return workingDirectory.relativize(originalFile.toAbsolutePath()).toString();
    }

    static class Result {
        private final String before;
        private final String after;

        public Result(String before, String after) {
            this.before = before;
            this.after = after;
        }
    }

    /**
     * Generate the filename with the sig generator for the given mediaFile and configured timezone.
     * @param mediaFile The input mediaFile
     * @return The generated signature
     */
    public String generateFilename(File mediaFile) {
        MetaData meta = metaDataService.metadataFromFile(mediaFile);
        String sig = sigGen.createUtcTimeZoneFilenameSig(meta);
        log.info("Capture datetime: {}. Result: {}.", meta.getCaptureDateTime(), sig);
        return sig;
    }
}
