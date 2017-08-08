package net.marmier.mediafilename.util.finder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Utility class for traversing directories and build list of encountered files. If a working directory is
 * provided, the resulting paths will be built relative to it.
 * <p>
 * Added by raphael on 08.08.17.
 */
public class Finder {

    private Logger log = LoggerFactory.getLogger(Finder.class);

    private Path workingDirectory;

    /**
     * Build a Finder instance that builds absolute paths.
     */
    public Finder() {}

    /**
     * Build a Finder instance that builds paths relative to the provided working directory, rather than absolute paths.
     * @param workingDirectory the working directory of reference
     */
    public Finder(File workingDirectory) {
        Objects.requireNonNull(workingDirectory, "Cannot operate on a null working directory.");
        if (workingDirectory.isDirectory()) {
            this.workingDirectory = workingDirectory.toPath().toAbsolutePath();
        }
        else {
            throw new FinderException(String.format("The file given as a working directory is not a directory: %s", workingDirectory));
        }
    }

    /**
     * Traverse the directory structure rooted at <code>file</code> and build a list of paths. If the
     * file is a not a directory, it is treated as a normal file and processed as such.
     * @param file the directory to traverse or single file to process.
     * @param ignoreErrors if <code>true</code>, continue after an error occurring while operating on a file.
     * @param ignoreNullResults if <code>true</code>, continue after a null result returned by the processor on a file.
     * @return the list of paths
     */
    public List<Path> find(File file, boolean ignoreErrors, boolean ignoreNullResults) {
        log.debug("Directory to traverse or file to relativize: {}", file);
        log.debug("Relative to working directory: {}", workingDirectory == null ? "<none provided>" : workingDirectory);
        log.debug("Ignore errors mode set to: {}", ignoreErrors);
        log.debug("Ignore null results mode set to: {}", ignoreNullResults);

        if (file.isDirectory()) {
            return find(file.toPath(), ignoreErrors, ignoreNullResults);
        } else {
            Path result = processFile(file.toPath());
            if (result != null) {
                return Collections.singletonList(result);
            } else {
                return Collections.emptyList();
            }
        }
    }

    private List<Path> find(Path directory, boolean ignoreErrors, boolean ignoreNullResults) {
        Objects.requireNonNull(directory, "No path provided for directory traversal.");

        FinderFileVisitor<Path> visitor = new FinderFileVisitor<>(this::processFile, ignoreErrors, ignoreNullResults);
        try {
            Files.walkFileTree(directory, visitor);
        } catch (IOException e) {
            String message = String.format("Error while traversing directory %s", directory);
            throw new FinderException(message, e);
        }
        return visitor.getResults();
    }

    private Path processFile(Path file) {
        Path absolutePath = file.toAbsolutePath();
        log.debug("Computing relative path for file: {}", absolutePath);

        if (workingDirectory != null) {
            return workingDirectory.relativize(absolutePath);
        } else {
            return file.getFileName();
        }
    }
}
