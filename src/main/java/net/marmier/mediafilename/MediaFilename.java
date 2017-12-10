package net.marmier.mediafilename;

import net.marmier.mediafilename.filename.FilenameHelper;
import net.marmier.mediafilename.timezone.Offset;
import net.marmier.mediafilename.ui.PhotoFilenameConverterFrame;
import net.marmier.mediafilename.util.finder.Finder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
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
        Offset offset;
        File targetFile;

        if (args.length < 1) {
            showUsage(null);
            System.exit(1);
        }
        String firstArgument = args[0];
        if (args.length == 1 && "--run-with-ui".equals(firstArgument)) {
            startUi();
        }
        else if (args.length < 2) {
            showUsage(null);
            System.exit(1);
        } else {
            // Decode the timezone offset
            offset = matchCode(firstArgument);

            // Get the fileArgumentName parameter
            String argument1 = args[1];
            targetFile = new File(argument1);

            realStart(offset, targetFile, new PrintStreamAppender(System.err), new PrintStreamAppender(System.out));
        }
    }

    private static void startUi() {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }

    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new PhotoFilenameConverterFrame();

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void realStart(Offset offset, File targetFile, OutputAppender err, OutputAppender out) throws IOException {
        LocalDateTime launchTime = LocalDateTime.now();

        // Determine the working directory. We fall back on the system property if that fails.
        String workingDirectory = getWorkingDirectory(targetFile);

        err.println("targetFile: " + targetFile);
        err.println("workingDirectory: " + workingDirectory);

        Path commandFile;

        // Assemble the output files basename
        String outputFilenameBase = String.format(
            "Importation_%s_%s_tz%s",
            launchTime.format(DateTimeFormatter.ISO_LOCAL_DATE),
            targetFile.getName(), offset.toString()
        );
        err.println("filenameBase: " + outputFilenameBase);

        // Prepare the command targetFile name
        commandFile = new File(workingDirectory + "/" + outputFilenameBase + ".sh").toPath();

        // Initialize the log fileArgumentName dynamically. This work provided we use non-static loggers.
        System.setProperty("logfilename", workingDirectory + "/" + outputFilenameBase);

        // Initialize the error log fileArgumentName dynamically. This work provided we use non-static loggers.
        System.setProperty("errorsfilename", workingDirectory + "/" + outputFilenameBase + "_errors");

        MediaFilename.workingDirectory = new File(workingDirectory).toPath();

        // Map to contain all entries, with the full original path as the key associated with the resulting filename.
        // Order must be preserved.
        Map<String, String> resultsByPath = new LinkedHashMap<>();
        // Map to contain results of the first pass by original filename without extension, so we can correlate
        // with secondary files and use the same new filename.
        Map<String, String> resultsByUniqueNames = new TreeMap<>();

        // Initialize the all files map with entries without result.
        final Finder finder = new Finder(new File(workingDirectory));
        List<Path> allFiles = finder.find(targetFile, false, false);

        // Initialize the service (last, so we can configure the log targetFile dynamically, just above)
        MediaProcessor mediaProcessor = new MediaProcessorImpl(offset, workingDirectory);

            /*
                First pass to process supported files
              */
        log.info("First pass: processing known extensions");

        // Process the media files and get the results
        final List<MediaProcessor.Result> results = mediaProcessor.process(allFiles);
        // Add just the result to the result map
        for (MediaProcessor.Result result : results) {
            log.debug("Caching result {} {}", result.getOriginalPath(), result.getNewFilename());
            resultsByPath.put(result.getOriginalPath().toString(), result.getNewFilename());

            resultsByUniqueNames.put(FilenameHelper.stripExtension(result.getOriginalPath().toFile().getName()), result.getNewFilename());
        }

        /*
            Second pass to match companion files to their master and use its new filename.
         */
        log.info("Second pass: matching companion files");

        for (Path filepath : allFiles) {
            if (!resultsByPath.containsKey(filepath.toString())) { // We proceed if the given file doesn't have a result yet.
                String nameWithoutExtension = FilenameHelper.stripExtension(filepath.toFile().getName());
                if (nameWithoutExtension == null || nameWithoutExtension.isEmpty()) {
                    err.println(String.format("Skipping file for which we could not determine base filename: %s", filepath.toString()));
                    continue;
                }
                String result = resultsByUniqueNames.get(nameWithoutExtension);
                if (result != null && !result.isEmpty()) {
                    log.info("Found result matching [{}] to [{}]", nameWithoutExtension, result);
                    String resultNameWithoutExtension = FilenameHelper.stripExtension(result);
                    if (resultNameWithoutExtension == null || resultNameWithoutExtension.isEmpty()) {
                        err.println(String.format("Skipping file for which we could not determine extension: %s.", filepath.toString()));
                        continue;
                    }
                    String extension = FilenameHelper.getExtension(filepath.toString());
                    String newfilename = resultNameWithoutExtension + "." + extension;
                    resultsByPath.put(filepath.toString(), newfilename);
                    log.info("Generated filename: {} {}", filepath.toString(), newfilename);
                } else {
                    err.println(String.format("No result matching %s for %s", nameWithoutExtension, filepath.toString()));
                }
            }
        }

        // Generate the command targetFile
        backupPreviousRunIfExist(commandFile);
        writeResult(commandFile, resultsByPath, out);
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

    private static Offset matchCode(String literalCode) {
        Offset offset = null;
        try {
            offset = Offset.forCode(literalCode);
        } catch (IllegalArgumentException e) {
            showUsage("Invalid timezone code provided: " + literalCode);
            System.exit(1);
        }
        return offset;
    }

    private static void writeResult(Path commandFile, Map<String, String> allFiles, OutputAppender out) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(commandFile, StandardOpenOption.CREATE_NEW)) {
            for (Map.Entry<String, String> result : allFiles.entrySet()) {
                String command = String.format("mv \'%s\' \'%s\'", result.getKey(), result.getValue());
                out.println(command);
                writer.write(command);
                writer.write("\n");
            }
        } catch (Exception e) {
            // Last ditch
            e.printStackTrace();
        }
    }

    private static void backupPreviousRunIfExist(Path commandFile) throws IOException {
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

    /**
     * Simple appender abstraction to allow us to log on different kind of output, like standard output and error when running
     * as a utility command, or like text areas when running in GUI.
     */
    public interface OutputAppender {
        void println(String line);
    }

    static class PrintStreamAppender implements OutputAppender {

        private PrintStream ps;

        public PrintStreamAppender(PrintStream ps) {
            Objects.requireNonNull(ps, "PrintStream required!");
            this.ps = ps;
        }
        @Override
        public void println(String line) {
            ps.println(line);
        }
    }
}
