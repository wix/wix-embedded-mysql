package com.wix.mysql.distribution.fileset;

import com.wix.mysql.distribution.WixVersion;

import de.flapdoodle.embed.process.config.store.FileSet;
import de.flapdoodle.embed.process.distribution.Platform;

import java.util.Objects;

import static de.flapdoodle.embed.process.config.store.FileType.Library;

public class Nix56FileSetEmitter extends Nix implements FileSetEmitter {
    @Override
    public boolean matches(Platform platform, WixVersion version) {
        return platform.isUnixLike() && Objects.equals(version.getMajorVersion(), "5.6");
    }

    @Override
    public FileSet emit() {
        return common()
                .addEntry(Library, "share/mysql_security_commands.sql")
                .addEntry(Library, "support-files/my-default.cnf")
                .addEntry(Library, "scripts/mysql_install_db")
                .build();
    }
}
