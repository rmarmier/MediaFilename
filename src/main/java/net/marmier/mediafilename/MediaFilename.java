package net.marmier.mediafilename;

import net.marmier.mediafilename.timezone.Offset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

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

    private Logger log = LoggerFactory.getLogger(MediaFilename.class);

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
            MediaProcessor mediaProcessor = new MediaProcessorImpl(offset, workingDirectory);

            // Process the media files and get the results
            final List<MediaProcessor.Result> results = mediaProcessor.process(targetFile);

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

    private static void writeResult(Path commandFile, List<MediaProcessor.Result> results) throws IOException {
        backupIfExist(commandFile);
        try (BufferedWriter writer = Files.newBufferedWriter(commandFile, StandardOpenOption.CREATE_NEW)) {
            for (MediaProcessor.Result result : results) {
                String command = String.format("mv \'%s\' \'%s\'", result.getBefore(), result.getAfter());
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
