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

    @Override
    public List<Result> process(List<Path> file) throws MediaProcessorException {
        return file.stream()
            .map(this::processFile)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    public Result processFile(Path file) throws MediaProcessorException {
        log.info("Processing {}", file.getFileName());

        String oldRelativeName = createOldRelativePath(file);

        String newName = generateFilename(file.toFile());
        if (newName != null) {
            String newRelativeName = createNewRelativePath(file, newName);
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
        private final String oldRelativeName;
        private final String newRelativeName;

        ResultImpl(String oldRelativeName, String newRelativeName) {
            this.oldRelativeName = oldRelativeName;
            this.newRelativeName = newRelativeName;
        }

        public String getOldRelativeName() {
            return oldRelativeName;
        }

        public String getNewRelativeName() {
            return newRelativeName;
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
