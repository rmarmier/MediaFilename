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
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static net.marmier.mediafilename.filename.FilenameHelper.stripExtension;

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

    /**
     * Process a list of file as described in {@link MediaProcessor#processFile(Path)}.
     *
     * @param file The List of absolute paths of files to process.
     * @return The list of result objects corresponding to the processed files
     * @throws MediaProcessorException various cases of error
     */
    @Override
    public List<Result> process(List<Path> file) throws MediaProcessorException {
        return file.stream()
            .map(this::processFile)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    /**
     * Process a single file.
     *
     * @param file Absolute path of file to process
     * @return a result object corresponding to the processed file
     * @throws MediaProcessorException various cases of error
     */
    @Override
    public Result processFile(Path file) throws MediaProcessorException {
        if (!file.isAbsolute()) {
            throw new MediaProcessorException(String.format("Expecting file path to be absolute: %s", file.toString()));
        }

        log.info("Processing {}", file.getFileName());

        String newName = generateFilename(file.toFile());
        if (newName != null) {
            String newRelativeName = createNewRelativePath(file, newName);
            return new ResultImpl(file, newRelativeName);
        }
        log.error("File could not be processed: ignored ({})", file.toString());
        return null;
    }

    private String createNewRelativePath(Path originalFile, String newName) {
        Path parentDir = originalFile.getParent();
        String newRelativeParent = workingDirectory.relativize(parentDir).toString();
        return String.format("%s%s", newRelativeParent + "/", newName);
    }

    private String createOldRelativePath(Path originalFile) {
        return workingDirectory.relativize(originalFile).toString();
    }

    static class ResultImpl implements Result {
        private final Path originalPath;
        private final String newFilename;

        ResultImpl(Path originalPath, String newFilename) {
            this.originalPath = originalPath;
            this.newFilename = newFilename;
        }

        public Path getOriginalPath() {
            return originalPath;
        }

        public String getNewFilename() {
            return newFilename;
        }

        public String getNewFilenameRoot() {
            return stripExtension(newFilename);
        }
    }

    /**
     * Generate the filename with the filename generator for the given mediaFile and configured timezone.
     * @param mediaFile The input mediaFile
     * @return The generated signature
     */
    @Override
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
