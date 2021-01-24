package com.wix.mysql.distribution.service;

import com.wix.mysql.config.MysqldConfig;
import com.wix.mysql.distribution.WixVersion;
import de.flapdoodle.embed.process.collections.Collections;
import de.flapdoodle.embed.process.extract.IExtractedFileSet;

import java.io.IOException;
import java.util.List;

public class UserProvidedArgumentsEmitter implements CommandEmitter {
    @Override
    public boolean matches(WixVersion version) {
        return true;
    }

    @Override
    public List<String> emit(MysqldConfig config, IExtractedFileSet exe) throws IOException {
        List<String> result = Collections.newArrayList();
        for (MysqldConfig.ServerVariable var: config.getServerVariables()) {
            result.add(var.toCommandLineArgument());
        }
        return result;
    }
}
