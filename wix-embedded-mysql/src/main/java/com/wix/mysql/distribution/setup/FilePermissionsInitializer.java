package com.wix.mysql.distribution.setup;

import com.wix.mysql.config.MysqldConfig;
import com.wix.mysql.distribution.WixVersion;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.config.store.FileType;
import de.flapdoodle.embed.process.distribution.Platform;
import de.flapdoodle.embed.process.extract.IExtractedFileSet;

import java.io.File;
import java.io.IOException;

public class FilePermissionsInitializer implements Initializer {
    @Override
    public boolean matches(WixVersion version) {
        return Platform.detect().isUnixLike();
    }

    @Override
    public void apply(IExtractedFileSet fileSet, IRuntimeConfig runtimeConfig, MysqldConfig config) throws IOException {
        for (File f : fileSet.files(FileType.Library)) {
            f.setExecutable(true);
        }
    }
}
