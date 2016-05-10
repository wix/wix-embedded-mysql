package com.wix.mysql.distribution.initializers;

import com.wix.mysql.distribution.Version;
import de.flapdoodle.embed.process.config.store.FileType;
import de.flapdoodle.embed.process.distribution.Platform;
import de.flapdoodle.embed.process.extract.IExtractedFileSet;

import java.io.File;
import java.io.IOException;

public class FilePermissionsInitializer implements Initializer {
    @Override
    public boolean matches(Version version) {
        return Platform.detect().isUnixLike();
    }

    @Override
    public void apply(IExtractedFileSet fileSet) throws IOException {
        for (File f : fileSet.files(FileType.Library)) {
            f.setExecutable(true);
        }
    }
}
