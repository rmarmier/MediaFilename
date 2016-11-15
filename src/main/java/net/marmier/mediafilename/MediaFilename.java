package net.marmier.mediafilename;

import net.marmier.mediafilename.filename.FilenameHelper;
import net.marmier.mediafilename.timezone.Offset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Generate new filenames for media files, so files from different sources can be archived together
 * with a consistent ordering. Please refer to the accompanying README.md for instructions.
 *
 * <h4>Version history</h4>
 * v 1.0 - 2016-02-26 - Initial usable version.
 *
 * Added by raphael on 29.11.15.
 */
public class MediaFilename {

    private static Logger log = LoggerFactory.getLogger(MediaFilename.class);

    private static Path workingDirectory;

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

            MediaFilename.workingDirectory = new File(workingDirectory).toPath();


            // Map to contain all entries, with the full original path as the key. Order must be preserved.
            Map<String, String> allFilesMap = new LinkedHashMap<>();
            // Map to contain result entries, with the original path stripped of extension as the key.
            Map<String, String> onlyResultsMap = new TreeMap<>();

            // Initialize the all files map with entries without result.
            for (String originalPath : scan(targetFile)) {
                allFilesMap.put(originalPath, null);
            }

            // Initialize the service (last, so we can configure the log targetFile dynamically, just above)
            MediaProcessor mediaProcessor = new MediaProcessorImpl(offset, workingDirectory);

            /*
                First pass to process supported files
              */
            log.info("First pass: processing known extensions");

            // Process the media files and get the results
            final List<MediaProcessor.Result> results = mediaProcessor.process(targetFile);
            // Add just the result to the result map
            for (MediaProcessor.Result result : results) {
                log.info("Caching result {} {}", result.getBefore(), result.getAfter());
                allFilesMap.put(result.getBefore(), result.getAfter());

                onlyResultsMap.put(FilenameHelper.stripExtension(result.getBefore()), result.getAfter());
            }

            /*
                Second pass to match companion files to their master and use its new filename.
             */
            log.info("Second pass: matching companion files");

            for (Map.Entry<String, String> entry : allFilesMap.entrySet()) {
                if (entry.getValue() == null) { // Select entries missing a result
                    String filepath = entry.getKey();
                    String pathWithoutExtension = FilenameHelper.stripExtension(filepath);
                    if (pathWithoutExtension == null || pathWithoutExtension.isEmpty()) {
                        System.err.println(String.format("Could not determine base filename of path: %s. Skipping.", entry.getKey()));
                        continue;
                    }
                    log.info("File: {} {}", entry.getKey(), pathWithoutExtension);
                    String result = onlyResultsMap.get(pathWithoutExtension);
                    log.info("Found result: {}", result);
                    if (result != null && !result.isEmpty()) { // Just to be safe
                        String resultFilenameCore = FilenameHelper.stripExtension(result);
                        if (resultFilenameCore == null || resultFilenameCore.isEmpty()) {
                            System.err.println(String.format("Could not determine base filename of path: %s. Skipping.", entry.getKey()));
                            continue;
                        }
                        String extension = FilenameHelper.getExtension(entry.getKey());
                        String newfilename = resultFilenameCore + "." + extension;
                        allFilesMap.put(entry.getKey(), newfilename);
                        log.info("Corresponding filename: {} {}", entry.getKey(), newfilename);
                    } else {
                        System.err.println(String.format("No matching filename for file: %s. Skipping.", entry.getKey()));
                    }
                }
            }

            // Generate the command targetFile
            backupIfExist(commandFile);
            writeResult(commandFile, allFilesMap);
        }
    }

    public static List<String> scan(File file) throws MediaProcessorException {
        if (file.isDirectory()) {
            return scanDirectory(file.toPath());
        } else {
            String result = scanFile(file.toPath());
            if (result != null) {
                return Collections.singletonList(result);
            }
            else {
                return Collections.emptyList();
            }
        }
    }

    public static List<String> scanDirectory(Path targetDirectory) throws MediaProcessorException {
        final List<String> results = new ArrayList<>();
        try {
            Files.walkFileTree(targetDirectory, new FileVisitor<Path>() {
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    String result = scanFile(file);
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
        } catch (IOException e) {
            throw new MediaProcessorException("An error occured while processing directory " + targetDirectory.toString(), e);
        }
        return results;
    }

    public static String scanFile(Path originalFile) throws MediaProcessorException {
        log.info("Collecting {}", originalFile.getFileName());

        return createOldRelativePath(originalFile);
    }

    private static String createOldRelativePath(Path originalFile) {
        return workingDirectory.relativize(originalFile.toAbsolutePath()).toString();
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

    private static void writeResult(Path commandFile, Map<String, String> allFiles) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(commandFile, StandardOpenOption.CREATE_NEW)) {
            for (Map.Entry<String, String> result : allFiles.entrySet()) {
                String command = String.format("mv \'%s\' \'%s\'", result.getKey(), result.getValue());
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
}
