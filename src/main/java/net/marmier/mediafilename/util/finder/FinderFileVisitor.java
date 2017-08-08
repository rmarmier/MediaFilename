package net.marmier.mediafilename.util.finder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Visitor class to use with {@link java.nio.file.Files#walkFileTree}, that applies
 * a processing function to each files as it goes and stops on processing error or <code>null<code> result, while
 * provinding a way to ignore those errors.
 * <p>
 * Added by raphael on 08.08.17.
 */
public class FinderFileVisitor<T> implements FileVisitor<Path> {
    private Logger log = LoggerFactory.getLogger(FinderFileVisitor.class);

    private final List<T> results;
    private final Function<Path, T> processor;
    private boolean ignoreErrors;
    private boolean tolerateNullResults;

    /**
     * Initialise a new instance.
     * @param processor the processor to use on each visited file
     */
    public FinderFileVisitor(Function<Path, T> processor) {
        this(processor, false, false);
    }

    /**
     * Initialise a new instance, overriding the processing options.
     * @param processor the processor to use on each visited file
     * @param ignoreErrors if <code>true</code> continue after a processing error (default <code>false</code>)
     * @param tolerateNullResults if <code>true</code> continue when the processing return <code>null</code> (default <code>false</code>)
     */
    public FinderFileVisitor(Function<Path, T> processor, boolean ignoreErrors, boolean tolerateNullResults) {
        this.results = new ArrayList<>();
        this.processor = processor;
        this.ignoreErrors = ignoreErrors;
        this.tolerateNullResults = tolerateNullResults;
    }

    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        final T result;
        try {
            result = processor.apply(file);
        }
        catch (Exception e) {
            if (ignoreErrors) {
                String message = String.format("Ignoring exception [%s] while processing file at path: %s", e.getMessage(), file);
                log.warn(message, e);
                return FileVisitResult.CONTINUE;
            }
            else {
                String message = String.format("Error processing file: exception [%s] while processing file at path: %s", e.getMessage(), file);
                throw new FinderException(message, e);
            }
        }
        if (result != null) {
            results.add(result);
        }
        else {
            if (tolerateNullResults) {
                log.info("Ignored null result for path: {}", file);
            }
            else {
                String message = String.format("Error processing file: unexpected null result for path: %s", file);
                throw new FinderException(message);
            }
        }
        return FileVisitResult.CONTINUE;
    }

    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        if (ignoreErrors) {
            String message = String.format("Ignored exception [%s] accessing file %s", exc.getMessage(), file);
            log.warn(message, exc);
            return FileVisitResult.CONTINUE;
        } else {
            String message = String.format("Error [%s] accessing file: %s", exc.getMessage(), file);
            throw new FinderException(message, exc);
        }
    }

    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    /**
     * @return the results of each file's processing
     */
    public List<T> getResults() {
        return results;
    }
}
