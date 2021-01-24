package com.wix.mysql.distribution.fileset;

import com.wix.mysql.distribution.WixVersion;

import de.flapdoodle.embed.process.config.store.FileSet;
import de.flapdoodle.embed.process.distribution.Platform;

public interface FileSetEmitter {
    boolean matches(final Platform platform, final WixVersion version);
    FileSet emit();
}
