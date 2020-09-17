package com.wix.mysql.distribution.fileset;

import com.wix.mysql.distribution.Version;
import de.flapdoodle.embed.process.config.store.FileSet;
import de.flapdoodle.embed.process.config.store.FileType;
import de.flapdoodle.embed.process.distribution.Platform;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static de.flapdoodle.embed.process.config.store.FileType.Executable;
import static de.flapdoodle.embed.process.config.store.FileType.Library;
import static de.flapdoodle.embed.process.distribution.Platform.OS_X;

public class Nix8GTE19FileSetEmitter extends Nix implements FileSetEmitter {

	private static Logger logger = LoggerFactory.getLogger(new Object() {
	}.getClass().getEnclosingClass());

	@Override
	public boolean matches(Platform platform, Version version) {
		boolean match = platform.isUnixLike() && (Platform.detect() != OS_X) && version.getMajorVersion().equals("8.0")
				&& version.getMinorVersion() >= 19;
		if (match) {
			System.out.println("conditions for applying \"" + this.getClass().getName() + "\" met.");
			logger.info("conditions for applying \"" + this.getClass().getName() + "\" met.");
		}
		return match;
	}

	@Override
	public FileSet emit() {
		return FileSet.builder()
				.addEntry(Executable, "bin/mysqld")
				.addEntry(Library, "bin/mysql")
				.addEntry(Library, "bin/mysqladmin")
				.addEntry(Library, "bin/my_print_defaults")
				.addEntry(Library, "share/english/errmsg.sys")
				.addEntry(FileType.Library, "lib/private/libssl.so.1.1")
				.addEntry(FileType.Library, "lib/private/libcrypto.so.1.1")
				.addEntry(FileType.Library, "lib/private/libprotobuf.so.3.11.4")
				.addEntry(FileType.Library, "lib/private/libprotobuf-lite.so.3.11.4").build();
	}
}
