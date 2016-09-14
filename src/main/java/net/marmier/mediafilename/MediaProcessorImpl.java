package net.marmier.mediafilename;

import net.marmier.mediafilename.filename.FilenameGenerator;
import net.marmier.mediafilename.metadata.MetaData;
import net.marmier.mediafilename.metadata.MetaDataService;
import net.marmier.mediafilename.metadata.exif.ExiftoolMetaDataService;
import net.marmier.mediafilename.metadata.exif.ExiftoolMetaDataServiceException;
import net.marmier.mediafilename.timezone.Offset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Added by raphael on 14.09.16.
 */
public class MediaProcessorImpl implements MediaProcessor {

    private Logger log = LoggerFactory.getLogger(MediaProcessorImpl.class);

    private MetaDataService metaDataService = new ExiftoolMetaDataService();

    private FilenameGenerator sigGen;

    private final Path workingDirectory;

    public MediaProcessorImpl(Offset code, String workingDirectory) {
        sigGen = new FilenameGenerator(code);
        this.workingDirectory = new File(workingDirectory).toPath();
    }

    public List<Result> process(File file) throws MediaProcessorException {
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

    public List<Result> processDirectory(Path targetDirectory) throws MediaProcessorException {
        final List<Result> results = new ArrayList<>();
        try {
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
        } catch (IOException e) {
            throw new MediaProcessorException("An error occured while processing directory " + targetDirectory.toString(), e);
        }
        return results;
    }

    public Result processFile(Path originalFile) throws MediaProcessorException {
        log.info("Processing {}", originalFile.getFileName());

        String oldRelativeName = createOldRelativePath(originalFile);

        String newName = generateFilename(originalFile.toFile());
        if (newName != null) {

            String newRelativeName = createNewRelativePath(originalFile, newName);

            return new ResultImpl(oldRelativeName, newRelativeName);
        }
        log.error("File could not be processed: ignored ({})", oldRelativeName);
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

    static class ResultImpl implements Result {
        private final String before;
        private final String after;

        public ResultImpl(String before, String after) {
            this.before = before;
            this.after = after;
        }

        public String getBefore() {
            return before;
        }

        public String getAfter() {
            return after;
        }
    }

    /**
     * Generate the filename with the filename generator for the given mediaFile and configured timezone.
     * @param mediaFile The input mediaFile
     * @return The generated signature
     */
    public String generateFilename(File mediaFile) throws MediaProcessorException {
        MetaData meta;
        try {
            meta = metaDataService.metadataFromFile(mediaFile);
        } catch (ExiftoolMetaDataServiceException e) {
            throw new MediaProcessorException("A problem occured while retrieving metadata from media file" + mediaFile.toString(), e);
        }
        if (meta == null) {
            return null;
        }
        String sig = sigGen.createUtcTimeZoneFilename(meta);
        log.info("Capture datetime: {}. Result: {}.", meta.getCaptureDateTime(), sig);
        return sig;
    }
}
