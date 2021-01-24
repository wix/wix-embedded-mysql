package com.wix.mysql.distribution.setup;

import com.wix.mysql.config.MysqldConfig;
import com.wix.mysql.distribution.WixVersion;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.distribution.Platform;
import de.flapdoodle.embed.process.extract.IExtractedFileSet;

import java.io.File;
import java.io.IOException;

import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

public class NixBefore57Initializer implements Initializer {
    @Override
    public boolean matches(WixVersion version) {
        return Platform.detect().isUnixLike() &&
                (version.getMajorVersion().equals("5.6") || version.getMajorVersion().equals("5.5"));
    }

    @Override
    public void apply(IExtractedFileSet files, IRuntimeConfig runtimeConfig, MysqldConfig config) throws IOException {
        File baseDir = files.baseDir();
        Process p = Runtime.getRuntime().exec(new String[]{
                        "scripts/mysql_install_db",
                        "--force",
                        "--no-defaults",
                        format("--basedir=%s", baseDir),
                        format("--datadir=%s/data", baseDir)},
                null,
                baseDir);

        new ProcessRunner("scripts/mysql_install_db").run(p, runtimeConfig, config.getTimeout(NANOSECONDS));
    }
}
