package com.wix.mysql.distribution

import java.lang.System._

import com.wix.mysql.distribution.Version._
import com.wix.mysql.exceptions.UnsupportedPlatformException
import de.flapdoodle.embed.process.distribution.Platform
import de.flapdoodle.embed.process.distribution.Platform._
import org.specs2.execute.{AsResult, Result}
import org.specs2.mutable.SpecWithJUnit
import org.specs2.specification.AroundEach

class VersionTest extends SpecWithJUnit with AroundEach {
  sequential

  "platform detection should detect" >> {
    "OS X" in {
      givenPlatformSetTo(OS_X)
      v5_7_15.asInDownloadPath mustEqual "/MySQL-5.7/mysql-5.7.15-osx10.11-x86_64"
    }

    "OS X 5.7.17+, 5.6.35+ and use different file scheme" in {
      givenPlatformSetTo(OS_X)

      v5_7_17.asInDownloadPath must contain( "macos" )
      v5_6_35.asInDownloadPath must contain( "macos" )
    }


    "Windows" in {
      givenPlatformSetTo(Windows)
      v5_7_15.asInDownloadPath mustEqual "/MySQL-5.7/mysql-5.7.15-winx64"
    }

    "Linux" in {
      givenPlatformSetTo(Linux)
      v5_7_15.asInDownloadPath mustEqual "/MySQL-5.7/mysql-5.7.15-linux-glibc2.5-x86_64"
      v5_7_18.asInDownloadPath mustEqual "/MySQL-5.7/mysql-5.7.18-linux-glibc2.5-x86_64"
      v5_7_19.asInDownloadPath mustEqual  "/MySQL-5.7/mysql-5.7.19-linux-glibc2.12-x86_64"
    }

  }

  "verify that" >> {
    "windows for 5.5.X is not supported" in {
      givenPlatformSetTo(Windows)
      v5_5_latest.asInDownloadPath must throwA[UnsupportedPlatformException]
    }

    "solaris is not supported" in {
      givenPlatformSetTo(Solaris)
      v5_5_latest.asInDownloadPath must throwA[UnsupportedPlatformException]
    }

    "freebsd is not supported" in {
      givenPlatformSetTo(FreeBSD)
      v5_5_latest.asInDownloadPath must throwA[UnsupportedPlatformException]
    }
  }

  def givenPlatformSetTo(platform: Platform): String = platform match {
    case Windows => setProperty("os.name", "Windows")
    case OS_X => setProperty("os.name", "Mac OS X")
    case Linux => setProperty("os.name", "Linux")
    case Solaris => setProperty("os.name", "SunOS")
    case FreeBSD => setProperty("os.name", "FreeBSD")
    case _ => throw new UnsupportedPlatformException("Unrecognized platform, currently not supported")
  }

  def around[R: AsResult](r: => R): Result = {
    val current = getProperty("os.name")
    try AsResult(r)
    finally setProperty("os.name", current)
  }
}
