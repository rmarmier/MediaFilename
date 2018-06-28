package net.marmier.mediafilename;

import net.marmier.mediafilename.filename.FilenameHelper;
import net.marmier.mediafilename.index.IndexedResultsHolder;
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
import java.util.List;
import java.util.Objects;

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
            showUsage(timeZoneMessage());
            System.exit(1);
        }
        String firstArgument = args[0];
        if (args.length == 1 && "--run-with-ui".equals(firstArgument)) {
            startUi();
        }
        else if (args.length < 2) {
            showUsage(timeZoneMessage());
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

        /*
            Determine the working directory effective for the job. The working directory will hold the resulting
            script and error files, named after the directory or the file processed. The resulting script can then
            easily be run inside a shell from within the working directory.

            Either the path provided is a full path, and we will use its immediate parent as working directory,
            either the path is relative and we will use the path provided by system property "user.dir"
            as working directory, expecting the provided path to be found immediately under it.
         */
        String workingDirectory;
        if (targetFile.isAbsolute()) {
            workingDirectory = targetFile.getParent();
            Objects.requireNonNull(workingDirectory); // Protect against a bad inconsistency. Should not happen.
        }
        else {
            workingDirectory = System.getProperty("user.dir");
            Objects.requireNonNull(workingDirectory,
                "System property user.dir is empty. Cannot determine an acceptable workingdirectory.");
            System.err.println("Using system 'user.dir' as working dir.");
        }

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

        // Working with the full path from now on.
        Path fullTargetPath;
        if (!targetFile.isAbsolute()) {
            fullTargetPath = new File(workingDirectory).toPath().resolve(targetFile.toPath());
        } else {
            fullTargetPath = targetFile.toPath();
        }

        // Initialize the all files map with entries without result.
        final Finder finder = new Finder();
        List<Path> allFiles = finder.find(fullTargetPath, false, false);

        // Initialize the service (last, so we can configure the log targetFile dynamically, just above)
        MediaProcessor mediaProcessor = new MediaProcessorImpl(offset, workingDirectory);

        /*
            First pass to process supported files
          */
        log.info("First pass: processing known extensions");

        // Process the media files and get the results
        List<MediaProcessor.Result> resultList = mediaProcessor.process(allFiles);

        // Index the results
        final IndexedResultsHolder indexedResults = new IndexedResultsHolder(resultList);

        /*
            Second pass to match companion files to their master and use their new filename.
         */
        log.info("Second pass: matching companion files");

        for (Path filepath : allFiles) {
            if (indexedResults.getResultMatchingPath(filepath) == null) { // We proceed if the given file doesn't have a result yet.
                final MediaProcessor.Result result = matchToExistingResult(err, indexedResults, filepath);
                if (result != null) {
                    final String newFilename = result.getNewFilenameRoot() + "." + FilenameHelper.getExtension(filepath.toString());
                    indexedResults.addResult(new MediaProcessorImpl.ResultImpl(filepath, newFilename));
                    log.info("Generated filename: {} {}", filepath.toString(), newFilename);
                }
            }
        }

        // Generate the command targetFile
        backupPreviousRunIfExist(commandFile);
        writeResult(commandFile, indexedResults.getResults(), out);
    }

    private static MediaProcessor.Result matchToExistingResult(OutputAppender err, IndexedResultsHolder results, Path filepath) {
        String filenameRoot = filenameRoot(filepath.toFile().getName());
        if (filenameRoot == null || filenameRoot.isEmpty()) {
            err.println(String.format("Skipping file for which we could not determine base filename: %s", filepath.toString()));
            return null;
        }
        MediaProcessor.Result resultMatchingRoot = results.getResultMatchingRoot(filepath);
        if (resultMatchingRoot != null) {
            log.info("Found result matching [{}] to [{}]", filenameRoot, resultMatchingRoot.getNewFilename());
            return resultMatchingRoot;
        }
        else {
            log.info("No result found matching [{}] for [{}]", filenameRoot, filepath.toString());
            return null;
        }
    }

    private static String filenameRoot(String filepath) {
        return FilenameHelper.stripExtension(filepath);
    }

    private static Offset matchCode(String literalCode) {
        Offset offset = null;
        try {
            offset = Offset.forCode(literalCode);
        } catch (IllegalArgumentException e) {
            String additionalInfo = "Invalid timezone code provided: " + literalCode + ".\n" + timeZoneMessage();
            showUsage(additionalInfo);
            System.exit(1);
        }
        return offset;
    }

    private static String timeZoneMessage() {
        return "Time zone can be any of: " + Offset.dumpOffsetCodes() + "\n";
    }

    private static void writeResult(Path commandFile, List<MediaProcessor.Result> allResults, OutputAppender out) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(commandFile, StandardOpenOption.CREATE_NEW)) {
            for (MediaProcessor.Result result : allResults) {
                String command = String.format("mv \'%s\' \'%s\'", workingDirectory.relativize(result.getOriginalPath()), result.getNewFilename());
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
        String usage = "Please provide a valid timezone (ex. +01:00) and the path to the directory containing the media to rename, \n"
            + "or use --run-with-ui to start the GUI.";
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
