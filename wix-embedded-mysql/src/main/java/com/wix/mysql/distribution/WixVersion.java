package com.wix.mysql.distribution;

import de.flapdoodle.embed.process.distribution.ArchiveType;
import de.flapdoodle.embed.process.distribution.IVersion;

public interface WixVersion extends IVersion {
  String getMajorVersion();

  int getMinorVersion();

  ArchiveType archiveType();

}
