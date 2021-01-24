package com.wix.mysql.distribution.fileset;

import com.wix.mysql.distribution.WixVersion;

import de.flapdoodle.embed.process.config.store.FileSet;
import de.flapdoodle.embed.process.distribution.Platform;

import java.util.Objects;

import static de.flapdoodle.embed.process.config.store.FileType.Library;

public class Nix55FileSetEmitter extends Nix implements FileSetEmitter {
    @Override
    public boolean matches(Platform platform, WixVersion version) {
        return platform.isUnixLike() && Objects.equals(version.getMajorVersion(), "5.5");
    }

    @Override
    public FileSet emit() {
        return common()
                .addEntry(Library, "scripts/mysql_install_db")
                .build();
    }
}
