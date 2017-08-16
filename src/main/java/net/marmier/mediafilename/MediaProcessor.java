package net.marmier.mediafilename;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

/**
 * Added by raphael on 14.09.16.
 */
public interface MediaProcessor {

    List<Result> process(List<Path> file) throws MediaProcessorException;

    Result processFile(Path originalFile) throws MediaProcessorException;

    String generateFilename(File mediaFile) throws MediaProcessorException;

    interface Result {
        String getOldRelativeName();
        String getNewRelativeName();
    }
}
