package net.marmier.mediafilename;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

/**
 * Added by raphael on 14.09.16.
 */
public interface MediaProcessor {

    List<Result> process(File file) throws MediaProcessorException;

    List<Result> processDirectory(Path targetDirectory) throws MediaProcessorException;

    Result processFile(Path originalFile) throws MediaProcessorException;

    String generateFilename(File mediaFile) throws MediaProcessorException;

    interface Result {
        public String getOldRelativeName();
        public String getNewRelativeName();
    }
}
