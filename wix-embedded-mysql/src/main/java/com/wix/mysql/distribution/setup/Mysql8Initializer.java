package com.wix.mysql.distribution.setup;

import com.wix.mysql.config.MysqldConfig;
import com.wix.mysql.distribution.Version;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.extract.IExtractedFileSet;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

public class Mysql8Initializer implements Initializer {
	
	private static Logger logger = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());
	
    @Override
    public boolean matches(Version version) {
        boolean match = version.getMajorVersion().equals("8.0");
        
    	if (match) {
    		logger.info("conditions for applying \"" + this.getClass().getName() + "\" met.");
    		System.out.println("conditions for applying \"" + this.getClass().getName() + "\" met.");
    	}
    	return match;
        
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
