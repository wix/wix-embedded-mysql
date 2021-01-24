package com.wix.mysql.distribution.service;

import com.wix.mysql.config.MysqldConfig;
import com.wix.mysql.distribution.WixVersion;

import de.flapdoodle.embed.process.collections.Collections;
import de.flapdoodle.embed.process.extract.IExtractedFileSet;

import java.io.IOException;
import java.util.List;

public class Mysql8CommandEmitter implements CommandEmitter {
    @Override
    public boolean matches(WixVersion version) {
        return version.getMajorVersion().equals("8.0");
    }

    @Override
    public List<String> emit(MysqldConfig config, IExtractedFileSet exe) throws IOException {
        return Collections
                .newArrayList("--default_authentication_plugin=mysql_native_password");
    }
}
