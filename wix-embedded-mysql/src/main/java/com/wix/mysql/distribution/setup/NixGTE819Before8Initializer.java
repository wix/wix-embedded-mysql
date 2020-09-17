package com.wix.mysql.distribution.setup;

import static de.flapdoodle.embed.process.distribution.Platform.OS_X;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wix.mysql.config.MysqldConfig;
import com.wix.mysql.distribution.Version;

import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.distribution.Platform;
import de.flapdoodle.embed.process.extract.IExtractedFileSet;

public class NixGTE819Before8Initializer implements Initializer {

        private static final String SEP = File.separator;
        private static Logger logger = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());
    
    @Override
    public boolean matches(Version version) {
    	boolean match = Platform.detect().isUnixLike() && (Platform.detect() != OS_X)
                && version.getMajorVersion().equals("8.0")
                && (version.getMinorVersion() >= 19);
    	if (match) {
    		System.out.println("conditions for applying \"" + this.getClass().getName() + "\" met.");
    		logger.info("conditions for applying \"" + this.getClass().getName() + "\" met.");
    	}
    	return match;
    }

    @Override
    public void apply(IExtractedFileSet files, IRuntimeConfig runtimeConfig, MysqldConfig config) throws IOException {
        File baseDir = files.baseDir();
        for (File file : new File(baseDir + SEP + "bin").listFiles()) {
        	System.out.println(file.getName() + " exists...");
        }
        File libDir = new File(baseDir + SEP + "lib" + SEP + "private");
        FileFilter filter = new RegexFileFilter("^[a-z\\|-]+\\.so(\\.[0-9]+)+");
        File[] soFiles = libDir.listFiles(filter);
        for (File file : soFiles) {
            Files.createSymbolicLink(Paths.get(baseDir + SEP + "bin" + SEP + file.getName()), Paths.get(file.getPath()));
            System.out.println("Symlink " + file.getName());
        }
    }
}
