package com.wix.mysql.distribution.fileset;

import com.wix.mysql.distribution.Version;
import de.flapdoodle.embed.process.config.store.FileSet;
import de.flapdoodle.embed.process.config.store.FileType;
import de.flapdoodle.embed.process.distribution.Platform;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static de.flapdoodle.embed.process.config.store.FileType.Executable;
import static de.flapdoodle.embed.process.config.store.FileType.Library;
import static de.flapdoodle.embed.process.distribution.Platform.OS_X;

public class Nix8LTE17FileSetEmitter extends Nix implements FileSetEmitter {

	private static Logger logger = LoggerFactory.getLogger(new Object() {
	}.getClass().getEnclosingClass());

	@Override
	public boolean matches(Platform platform, Version version) {
		boolean match = platform.isUnixLike() && (Platform.detect() != OS_X) && version.getMajorVersion().equals("8.0")
				&& (version.getMinorVersion() <= 17);
		if (match) {
			System.out.println("conditions for applying \"" + this.getClass().getName() + "\" met.");
			logger.info("conditions for applying \"" + this.getClass().getName() + "\" met.");
		}
		return match;
	}

	@Override
	public FileSet emit() {
		return FileSet.builder().addEntry(Executable, "bin/mysqld").addEntry(Library, "bin/mysql")
				.addEntry(Library, "bin/mysqladmin").addEntry(Library, "bin/my_print_defaults")
				.addEntry(Library, "share/english/errmsg.sys").addEntry(FileType.Library, "lib/libssl.so.1.0.0")
				.addEntry(FileType.Library, "lib/libcrypto.so.1.0.0").build();
	}
}
