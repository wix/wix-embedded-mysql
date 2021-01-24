package com.wix.mysql.distribution.fileset;

import com.wix.mysql.distribution.WixVersion;

import de.flapdoodle.embed.process.config.store.FileSet;
import de.flapdoodle.embed.process.distribution.Platform;

import java.util.Objects;

import static de.flapdoodle.embed.process.config.store.FileType.Executable;
import static de.flapdoodle.embed.process.config.store.FileType.Library;

public class Win57_18_UpFileSetEmitter implements FileSetEmitter {
    @Override
    public boolean matches(Platform platform, WixVersion version) {
        return !platform.isUnixLike()
                && Objects.equals(version.getMajorVersion(), "5.7")
                && version.getMinorVersion() > 17;
    }

    @Override
    public FileSet emit() {
        return FileSet.builder()
                .addEntry(Executable, "bin/mysqld.exe")
                .addEntry(Library, "bin/mysql.exe")
                .addEntry(Library, "bin/mysqladmin.exe")
                .addEntry(Library, "share/english/errmsg.sys")
                .addEntry(Library, "share/fill_help_tables.sql")
                .addEntry(Library, "share/mysql_security_commands.sql")
                .addEntry(Library, "share/mysql_sys_schema.sql")
                .addEntry(Library, "share/mysql_system_tables.sql")
                .addEntry(Library, "share/mysql_system_tables_data.sql")
                .build();
    }
}
