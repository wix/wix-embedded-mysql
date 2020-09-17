package com.wix.mysql.distribution.setup;

import static de.flapdoodle.embed.process.distribution.Platform.OS_X;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

import org.apache.commons.io.filefilter.RegexFileFilter;

import com.wix.mysql.config.MysqldConfig;
import com.wix.mysql.distribution.Version;

import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.distribution.Platform;
import de.flapdoodle.embed.process.extract.IExtractedFileSet;

public class NixLTE818Before8Initializer implements Initializer {

        private static final String SEP = File.separator;
    
    @Override
    public boolean matches(Version version) {
    	return Platform.detect().isUnixLike() && (Platform.detect() != OS_X)
                && Objects.equals(version.getMajorVersion(), "8.0")
                && (version.getMinorVersion() <= 18);
    }

    @Override
    public void apply(IExtractedFileSet files, IRuntimeConfig runtimeConfig, MysqldConfig config) throws IOException {
        File baseDir = files.baseDir();
        File libDir = new File(baseDir + SEP + "lib");
        FileFilter filter = new RegexFileFilter("^[a-z]+\\.so(\\.[0-9]+)+");
        File[] soFiles = libDir.listFiles(filter);
        for (File file : soFiles) {
            Files.createSymbolicLink(Paths.get(baseDir + SEP + "bin" + SEP + file.getName()), Paths.get(file.getPath()));
            System.out.println("Symlink " + file.getName());
        }
    }
}
