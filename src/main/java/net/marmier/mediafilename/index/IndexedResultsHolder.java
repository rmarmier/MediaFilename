package net.marmier.mediafilename.index;

import net.marmier.mediafilename.MediaProcessor;
import net.marmier.mediafilename.filename.FilenameHelper;

import java.nio.file.Path;
import java.util.*;

/**
 * Added by raphael on 01.04.18.
 */
public class IndexedResultsHolder {

    private final List<MediaProcessor.Result> results;

    // Map to contain all entries, with the full original path as the key associated with the resulting filename.
    // Order must be preserved.
    private final Map<String, MediaProcessor.Result> byPathIndex = new LinkedHashMap<>();

    // Map to contain results of the first pass by original filename without extension (root), so we can correlate
    // with secondary files and use the same new filename.
    private final Map<String, MediaProcessor.Result> byRootIndex = new TreeMap<>();

    public IndexedResultsHolder(List<MediaProcessor.Result> results) {
        this.results = new ArrayList<>(results);

        // Index the result
        for (MediaProcessor.Result result : results) {
            indexResult(result);
        }
    }

    public MediaProcessor.Result getResultMatchingPath(Path path) {
        return byPathIndex.get(path.toString());
    }

    public MediaProcessor.Result getResultMatchingRoot(Path path) {
        String queryRoot = getFilenameRoot(path);
        return queryRoot == null ? null : byRootIndex.get(queryRoot);
    }

    public void addResult(MediaProcessor.Result result) {
        this.results.add(result);
        indexResult(result);
    }

    private void indexResult(MediaProcessor.Result result) {

        Objects.requireNonNull(result);
        String originalPath = result.getOriginalPath().toString();
        byPathIndex.putIfAbsent(originalPath, result);

        String filenameRoot = getFilenameRoot(result.getOriginalPath());
        Objects.requireNonNull(filenameRoot);
        byRootIndex.putIfAbsent(filenameRoot, result);
    }

    private String getFilenameRoot(Path originalPath) {
        return FilenameHelper.stripExtension(originalPath.toFile().getName());
    }

    public List<MediaProcessor.Result> getResults() {
        return Collections.unmodifiableList(results);
    }
}
