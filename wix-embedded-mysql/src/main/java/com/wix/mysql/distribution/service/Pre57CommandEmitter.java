package com.wix.mysql.distribution.service;

import com.wix.mysql.config.MysqldConfig;
import com.wix.mysql.distribution.WixVersion;

import de.flapdoodle.embed.process.collections.Collections;
import de.flapdoodle.embed.process.extract.IExtractedFileSet;

import java.io.IOException;
import java.util.List;

public class Pre57CommandEmitter implements CommandEmitter {
    @Override
    public boolean matches(WixVersion version) {
        return version.getMajorVersion().equals("5.5") || version.getMajorVersion().equals("5.6");
    }

    @Override
    public List<String> emit(MysqldConfig config, IExtractedFileSet exe) throws IOException {
        return Collections.newArrayList("--skip-name-resolve");
    }
}
