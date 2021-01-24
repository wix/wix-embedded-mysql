package com.wix.mysql;

import com.wix.mysql.distribution.WixVersion;

import de.flapdoodle.embed.process.config.store.FileSet;
import de.flapdoodle.embed.process.config.store.IPackageResolver;
import de.flapdoodle.embed.process.distribution.ArchiveType;
import de.flapdoodle.embed.process.distribution.BitSize;
import de.flapdoodle.embed.process.distribution.Distribution;
import static java.lang.String.format;
import static de.flapdoodle.embed.process.distribution.ArchiveType.TXZ;

public class PackagePaths implements IPackageResolver {
    @Override
    public FileSet getFileSet(Distribution distribution) {
        return com.wix.mysql.distribution.FileSet.emit(
                distribution.getPlatform(),
                (WixVersion) distribution.getVersion());
    }

    @Override
    public ArchiveType getArchiveType(Distribution distribution) {
        WixVersion version = (WixVersion)distribution.getVersion();
        return version.archiveType();
    }

    @Override
    public String getPath(Distribution distribution) {
        String downloadPath = distribution.getVersion().asInDownloadPath();
        WixVersion version = (WixVersion)distribution.getVersion();

        BitSize bs = distribution.getBitsize();
        switch (distribution.getPlatform()) {
            case OS_X:
                return format("%s.tar.gz", downloadPath);
            case Linux:
                String gzOrXz = version.archiveType() == TXZ ? "xz" : "gz";
                return format("%s.tar.%s", downloadPath, gzOrXz);
            case Windows:
                return format("%s.zip", downloadPath);
            default:
                throw new RuntimeException("Not implemented for: " + distribution.getPlatform());
        }
    }
}
