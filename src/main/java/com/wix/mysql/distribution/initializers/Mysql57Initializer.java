package com.wix.mysql.distribution.initializers;

import com.wix.mysql.distribution.Version;
import de.flapdoodle.embed.process.extract.IExtractedFileSet;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

import static java.lang.String.format;

public class Mysql57Initializer implements Initializer {
    @Override
    public boolean matches(Version version) {
        return version.getMajorVersion().equals("5.7");
    }

    @Override
    public void apply(IExtractedFileSet files) throws IOException {
        File baseDir = files.baseDir();
        // TODO: wait until https://github.com/flapdoodle-oss/de.flapdoodle.embed.process/pull/41 is merged
        FileUtils.deleteDirectory(new File(baseDir, "data"));

        Process p = Runtime.getRuntime().exec(new String[]{
                        files.executable().getAbsolutePath(),
                        "--no-defaults",
                        "--initialize-insecure",
                        format("--basedir=%s", baseDir),
                        format("--datadir=%s/data", baseDir)});

        ProcessRunner.run(p);
    }
}
