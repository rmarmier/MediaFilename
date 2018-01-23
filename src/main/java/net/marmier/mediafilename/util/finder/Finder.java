package net.marmier.mediafilename.util.finder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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


    /**
     * Build a Finder instance
     */
    public Finder() {}

    /**
     * Traverse the directory structure rooted at <code>path</code> and return a list of paths. If the
     * path is a not a directory, it is merely returned.
     * @param path the directory to traverse.
     * @param ignoreErrors if <code>true</code>, continue after an error occurring while operating on a path.
     * @param ignoreNullResults if <code>true</code>, continue after a null result returned by the processor on a path.
     * @return the list of paths
     */
    public List<Path> find(Path path, boolean ignoreErrors, boolean ignoreNullResults) {
        log.debug("Directory to traverse or path to relativize: {}", path);
        log.debug("Ignore errors mode set to: {}", ignoreErrors);
        log.debug("Ignore null results mode set to: {}", ignoreNullResults);

        if (Files.isDirectory(path)) {
            Objects.requireNonNull(path, "No path provided for directory traversal.");

            FinderFileVisitor<Path> visitor = new FinderFileVisitor<>(file -> file, ignoreErrors, ignoreNullResults);
            try {
                Files.walkFileTree(path, visitor);
            } catch (IOException e) {
                String message = String.format("Error while traversing directory %s", path);
                throw new FinderException(message, e);
            }
            return visitor.getResults();
        } else {
            return Collections.singletonList(path);
        }
    }
}
