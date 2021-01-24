package com.wix.mysql.distribution.setup;

import com.wix.mysql.config.MysqldConfig;
import com.wix.mysql.distribution.WixVersion;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.extract.IExtractedFileSet;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

public class Mysql8Initializer implements Initializer {
    @Override
    public boolean matches(WixVersion version) {
        return version.getMajorVersion().equals("8.0");
    }

    @Override
    public void apply(IExtractedFileSet files, IRuntimeConfig runtimeConfig, MysqldConfig config) throws IOException {
        File baseDir = files.baseDir();
        FileUtils.deleteDirectory(new File(baseDir, "data"));

        Process p = Runtime.getRuntime().exec(new String[]{
                files.executable().getAbsolutePath(),
                "--no-defaults",
                "--initialize-insecure",
                format("--basedir=%s", baseDir),
                format("--datadir=%s/data", baseDir)});

        new ProcessRunner(files.executable().getAbsolutePath()).run(p, runtimeConfig, config.getTimeout(NANOSECONDS));
    }
}
