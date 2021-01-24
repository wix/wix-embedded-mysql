package com.wix.mysql.distribution

import java.lang.System.{getProperty, setProperty}

import com.wix.mysql.exceptions.UnsupportedPlatformException
import org.specs2.execute.{AsResult, Result}
import org.specs2.mutable.{Around, SpecWithJUnit}
import org.specs2.specification.AroundEach
import org.specs2.specification.core.Fragment

class MacOsSierraTest extends SpecWithJUnit with Around with AroundEach {
  sequential

  val unsupportedVersions: Array[Version] = Version.values filter (!_.supportsCurrentPlatform)

  Fragment.foreach(unsupportedVersions) { version =>

    s"$version should fail on Sierra with helpful message" in {

      version.asInDownloadPath() must throwAn[UnsupportedPlatformException](
        message = s"$version is not supported on Mac OS Sierra. Minimum supported version is 5.7.15"
      )
    }
  }

  def around[R: AsResult](r: => R): Result = {
    val currentOsName = getProperty("os.name")
    val currentOsVersion = getProperty("os.version")

    setProperty("os.name", "Mac OS X")
    setProperty("os.version", "10.12")

    try AsResult(r)
    finally {
      setProperty("os.name", currentOsName)
      setProperty("os.version", currentOsVersion)
    }
  }
}
