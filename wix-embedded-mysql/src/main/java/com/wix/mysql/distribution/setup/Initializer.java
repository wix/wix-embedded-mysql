package com.wix.mysql.distribution.setup;


import com.wix.mysql.config.MysqldConfig;
import com.wix.mysql.distribution.WixVersion;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.extract.IExtractedFileSet;

import java.io.IOException;

public interface Initializer {
    boolean matches(WixVersion version);

    void apply(IExtractedFileSet files, IRuntimeConfig runtimeConfig, MysqldConfig config) throws IOException;
}
