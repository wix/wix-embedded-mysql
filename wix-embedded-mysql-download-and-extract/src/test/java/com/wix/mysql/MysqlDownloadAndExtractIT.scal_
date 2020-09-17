package com.wix.mysql

import java.io.File
import java.nio.file.{Files, Path, Paths}

import org.apache.commons.io.FileUtils.deleteDirectory
import org.specs2.matcher.{FileMatchers, Matchers}
import org.specs2.mutable.SpecWithJUnit

import scala.collection.JavaConverters.iterableAsScalaIterableConverter

class MysqlDownloadAndExtractIT extends SpecWithJUnit with Matchers with FileMatchers {

  "MysqlDownloadAndExtract" should {

    "store download-and-extract cache in custom location" in {
      val someVersion = "5.6"
      withTempDir { tempDir =>

        run(mysqlDownloadAndExtractDeployable, tempDir, someVersion) == Success

        extractedInstallersFolder(tempDir) must beADirectory and exist
      }
    }

    "download-and-extract artifact according to input version" >> {

      "uses the major version" in {
        val majorVersion = "5.7"
        withTempDir { tempDir =>

          run(mysqlDownloadAndExtractDeployable, tempDir, s"$majorVersion") == Success

          findMajorVersionDir(tempDir, majorVersion) must beRight[File]
        }
      }

      "uses the minor version" in {
        val someMajorVersion = "5.6"
        val minorVersion = "35"
        withTempDir { tempDir =>

          run(mysqlDownloadAndExtractDeployable, tempDir, someMajorVersion, minorVersion) == Success

          findMinorVersionDir(tempDir, someMajorVersion, minorVersion) must beRight[File]
        }
      }
    }

  }
  private val Success = 0
  private def findMajorVersionDir(basedir: String, majorVersion: String): Either[String, File] =
    installersContainerFolder(basedir).right.map(findMajorVersionDir(_, majorVersion)).joinRight

  private def findMajorVersionDir(installersContainerFolder: File, majorVersion: String) = {
    val installersFolders = installersContainerFolder.listFiles()
    val maybeMajorVersionFolder = installersFolders.find(_.getName.contains(majorVersion))
    maybeMajorVersionFolder.toRight(s"Download folder (${installersContainerFolder.getAbsolutePath}) " +
      s"did not contain a folder for *major* version ($majorVersion), children = ${installersFolders.toList}")
  }

  private def installersContainerFolder(basedir: String) =
    osSpecificChildFolder(extractedInstallersFolder(basedir))

  private def osSpecificChildFolder(downloadFolder: File) =
    downloadFolder.listFiles().toList.headOption.toRight(s"expected a file under ${downloadFolder.getAbsolutePath} but got none")

  private def extractedInstallersFolder(basedir: String): File = new File(basedir, "extracted")

  private def findMinorVersionDir(basedir: String, majorVersion: String, minorVersion: String): Either[String, File] = {
    findMajorVersionDir(basedir, majorVersion).right.map { majorVersionFolder =>
      val minorVersionFolders = majorVersionFolder.listFiles()
      val maybeMinorVersionFolder = minorVersionFolders.find(_.getName.contains(s"$majorVersion.$minorVersion"))
      maybeMinorVersionFolder.toRight(s"Major version folder (${majorVersionFolder.getAbsolutePath}) exists " +
        s"but did not contain a folder for *minor* version ($minorVersion), children = ${minorVersionFolders.toList}")
    }.joinRight
  }

  def withTempDir[T](f: String => T): T = {
    val tempDir = Files.createTempDirectory("embed-mysql-test").toFile

    try {
      f(tempDir.getAbsolutePath)
    } finally {
      deleteDirectory(tempDir)
    }
  }

  private def run(runnable: Path, args: String*): Int = {
    import sys.process._
    ("java" +: "-jar" +: runnable.toAbsolutePath.toString +: args).mkString(" ").!
  }

  private def mysqlDownloadAndExtractDeployable: Path = {
    val targetDir = Paths.get("./target")
    val maybePath = if (Files.exists(targetDir) && Files.isDirectory(targetDir)) {
      Files.newDirectoryStream(targetDir).asScala.filter(Files.isRegularFile(_))
        .find(_.toString.endsWith("jar-with-dependencies.jar"))
    } else {
      None
    }
    maybePath.getOrElse(throw new RuntimeException(
      """
        |!!! ERROR !!! jar-ball artifact with name wix-embedded-mysql-download-and-extract-some-version-jar-with-dependencies.jar cannot be found. Please verify the following:
        |1. The project is built and packaged using Maven: '$$ mvn package' should do the trick
        |2. Your working directory is set to the module directory ($$MODULE_DIR$$)
      """.stripMargin))
  }

}