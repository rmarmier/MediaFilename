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
import java.util.stream.Collectors;

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
            offset = decodeCode(firstArgument);

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
        String filenameBase = String.format(
            "Importation_%s_%s_tz%s",
            launchTime.format(DateTimeFormatter.ISO_LOCAL_DATE),
            targetFile.getName(), offset.toString()
        );
        err.println("filenameBase: " + filenameBase);

        // Prepare the command targetFile name
        commandFile = new File(workingDirectory + "/" + filenameBase + ".sh").toPath();

        // Initialize the log fileArgumentName dynamically. This work provided we use non-static loggers.
        System.setProperty("logfilename", workingDirectory + "/" + filenameBase);

        // Initialize the error log fileArgumentName dynamically. This work provided we use non-static loggers.
        System.setProperty("errorsfilename", workingDirectory + "/" + filenameBase + "_errors");

        MediaFilename.workingDirectory = new File(workingDirectory).toPath();

        // Map to contain all entries, with the full original path as the key. Order must be preserved.
        Map<String, String> allFilesByPath = new LinkedHashMap<>();
        // Map to contain result entries, with the original path stripped of extension as the key.
        Map<String, String> resultsByPath = new TreeMap<>();

        // Initialize the all files map with entries without result.
        final Finder finder = new Finder(new File(workingDirectory));
        for (Path originalPath : finder.find(targetFile, false, false)) {
            allFilesByPath.put(originalPath.toString(), null);
        }

        // Initialize the service (last, so we can configure the log targetFile dynamically, just above)
        MediaProcessor mediaProcessor = new MediaProcessorImpl(offset, workingDirectory);

            /*
                First pass to process supported files
              */
        log.info("First pass: processing known extensions");

        List<Path> allFiles = allFilesByPath.keySet().stream()
            .map(File::new)
            .map(File::toPath)
            .collect(Collectors.toList());

        // Process the media files and get the results
        final List<MediaProcessor.Result> results = mediaProcessor.process(allFiles);
        // Add just the result to the result map
        for (MediaProcessor.Result result : results) {
            log.info("Caching result {} {}", result.getOldRelativeName(), result.getNewRelativeName());
            allFilesByPath.put(result.getOldRelativeName(), result.getNewRelativeName());

            resultsByPath.put(FilenameHelper.stripExtension(result.getOldRelativeName()), result.getNewRelativeName());
        }

            /*
                Second pass to match companion files to their master and use its new filename.
             */
        log.info("Second pass: matching companion files");

        for (Map.Entry<String, String> entry : allFilesByPath.entrySet()) {
            if (entry.getValue() == null) { // Select entries missing a result
                String filepath = entry.getKey();
                String pathWithoutExtension = FilenameHelper.stripExtension(filepath);
                if (pathWithoutExtension == null || pathWithoutExtension.isEmpty()) {
                    err.println(String.format("Could not determine base filename of path: %s. Skipping.", entry.getKey()));
                    continue;
                }
                log.info("File: {} {}", entry.getKey(), pathWithoutExtension);
                String result = resultsByPath.get(pathWithoutExtension);
                log.info("Found result: {}", result);
                if (result != null && !result.isEmpty()) { // Just to be safe
                    String resultFilenameCore = FilenameHelper.stripExtension(result);
                    if (resultFilenameCore == null || resultFilenameCore.isEmpty()) {
                        err.println(String.format("Could not determine base filename of path: %s. Skipping.", entry.getKey()));
                        continue;
                    }
                    String extension = FilenameHelper.getExtension(entry.getKey());
                    String newfilename = resultFilenameCore + "." + extension;
                    allFilesByPath.put(entry.getKey(), newfilename);
                    log.info("Corresponding filename: {} {}", entry.getKey(), newfilename);
                } else {
                    err.println(String.format("No matching filename for file: %s. Skipping.", entry.getKey()));
                }
            }
        }

        // Generate the command targetFile
        backupIfExist(commandFile);
        writeResult(commandFile, allFilesByPath, out);
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
