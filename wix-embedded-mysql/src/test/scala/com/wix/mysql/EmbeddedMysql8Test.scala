package com.wix.mysql

import java.io.File
import java.util.concurrent.TimeUnit

import com.wix.mysql.EmbeddedMysql._
import com.wix.mysql.ScriptResolver.classPathScript
import com.wix.mysql.config.Charset._
import com.wix.mysql.config.DownloadConfig.aDownloadConfig
import com.wix.mysql.config.MysqldConfig.{SystemDefaults, aMysqldConfig}
import com.wix.mysql.config.ProxyFactory.aHttpProxy
import com.wix.mysql.config.SchemaConfig.aSchemaConfig
import com.wix.mysql.distribution.Version
import com.wix.mysql.distribution.Version.{v8_latest}
import com.wix.mysql.exceptions.CommandFailedException
import com.wix.mysql.support.IntegrationTest._
import com.wix.mysql.support.{HttpProxyServerSupport, IntegrationTest}

import scala.collection.JavaConverters._


class EmbeddedMysql8Test extends IntegrationTest with HttpProxyServerSupport {

  "EmbeddedMysql instance" should {  
    
    "start with default values" in {
      val config = testConfigBuilder(v8_latest).build

      val mysqld = start(anEmbeddedMysql(config))

      mysqld must
//        haveCharsetOf(UTF8MB4) and
        beAvailableOn(3310, "auser", "sa", SystemDefaults.SCHEMA) and
        haveServerTimezoneMatching("UTC") //and
//        haveSystemVariable("basedir", contain(pathFor("/target/", "/mysql-8.0-")))
    }

    "use custom values provided via MysqldConfig" in {
      val tempDir = System.getProperty("java.io.tmpdir")
      val config = testConfigBuilder(v8_latest)
//        .withCharset(LATIN1)
        .withUser("zeUser", "zePassword")
        .withPort(1112)
        .withTimeZone("US/Michigan")
        .withTempDir(tempDir)
        .build

      val mysqld = start(anEmbeddedMysql(config))

      mysqld must
//        haveCharsetOf(LATIN1) and
        beAvailableOn(1112, "zeUser", "zePassword", SystemDefaults.SCHEMA) and
        haveServerTimezoneMatching("US/Michigan") //and
//        haveSystemVariable("basedir", contain(pathFor(tempDir, "/mysql-8.0-")))
    }
/*
    "allow to provide network proxy" in {
      val targetVersion = Version.v8_latest
      anEmbeddedMysql(targetVersion).start().stop()

      withProxyOn(3210, 3220, targetVersion) { (proxy, proxyPort, targetPort, version) =>
        val config = aMysqldConfig(version).build

        val mysqld = start(anEmbeddedMysql(config)
          .withDownloadConfig(aDownloadConfig()
            .withProxy(aHttpProxy("127.0.0.1", proxyPort))
            .withBaseUrl(s"http://localhost:$targetPort")
            .build())
          .addSchema("aschema"))

        mysqld must beAvailableOn(config, "aschema")
        proxy.wasDownloaded must beTrue
      }
    }

    "accept system variables" in {
      val config = testConfigBuilder(v8_latest)
        .withServerVariable("max_connect_errors", 666)
        .build

      val mysqld = start(anEmbeddedMysql(config))

      mysqld must haveSystemVariable("max_connect_errors", ===("666"))
    }
*/
    "not allow to override library-managed system variables" in {
      val config = testConfigBuilder(v8_latest)
        .withTimeZone("US/Michigan")
        .withServerVariable("default-time-zone", "US/Eastern")
        .build

      start(anEmbeddedMysql(config)) must throwA[RuntimeException]
    }

    "respect provided timeout" in {
      start(anEmbeddedMysql(aMysqldConfig(v8_latest).withTimeout(10, TimeUnit.MILLISECONDS).build)) must
        throwA[RuntimeException].like { case e => e.getMessage must contain("0 sec") }
    }
  }

  "EmbeddedMysql schema reload" should {
    "reset schema" in {
      val mysqldConfig = testConfigBuilder(v8_latest).build
      val schemaConfig = aSchemaConfig("aSchema")
        .withScripts(aMigrationWith("create table t1 (col1 INTEGER);"))
        .build

      val mysqld = start(anEmbeddedMysql(mysqldConfig).addSchema(schemaConfig))

      anUpdate(mysqld, onSchema = "aSchema", sql = "insert into t1 values(10)") must beSuccessful
      aSelect[java.lang.Long](mysqld, onSchema = "aSchema", sql = "select col1 from t1 where col1 = 10;") mustEqual 10

      mysqld.reloadSchema(schemaConfig)

      aSelect[java.lang.Long](mysqld, onSchema = "aSchema", sql = "select col1 from t1 where col1 = 10;") must
        failWith("Incorrect result size: expected 1, actual 0")
    }
  }

  "EmbeddedMysql schema creation" should {
    "use defaults" in {
      val config = testConfigBuilder(v8_latest).build

      val mysqld = start(anEmbeddedMysql(config).addSchema("aSchema"))

      mysqld must
//        haveSchemaCharsetOf(UTF8MB4, "aSchema") and
        beAvailableOn(3310, "auser", "sa", andSchema = "aSchema")
    }

    "use custom values" in {
      val config = testConfigBuilder(v8_latest).build
      val schema = aSchemaConfig("aSchema")
        .withCharset(LATIN1)
        .build

      val mysqld = start(anEmbeddedMysql(config).addSchema(schema))

      mysqld must
//        haveSchemaCharsetOf(LATIN1, "aSchema") and
        beAvailableOn(3310, "auser", "sa", andSchema = "aSchema")
    }

    "inherit schema charset from instance" in {
      val config = testConfigBuilder(v8_latest).withCharset(LATIN1).build
      val schema = aSchemaConfig("aSchema").build

      val mysqld = start(anEmbeddedMysql(config).addSchema(schema))

      mysqld must
//        haveSchemaCharsetOf(LATIN1, "aSchema") and
        beAvailableOn(3310, "auser", "sa", andSchema = "aSchema")
    }

    "apply migrations when providing single file" in {
      val mysqld = start(anEmbeddedMysql(testConfigBuilder(v8_latest).build)
        .addSchema("aSchema", aMigrationWith("create table t1 (col1 INTEGER);")))

      aQuery(mysqld, onSchema = "aSchema", sql = "select count(col1) from t1;") must beSuccessful
    }

    "apply migrations from multiple files" in {
      val mysqld = start(anEmbeddedMysql(testConfigBuilder(v8_latest).build)
        .addSchema("aSchema",
          aMigrationWith("create table t1 (col1 INTEGER);"),
          aMigrationWith("create table t2 (col1 INTEGER);"))
      )

      aQuery(mysqld, onSchema = "aSchema", sql = "select count(col1) from t1;") must beSuccessful
      aQuery(mysqld, onSchema = "aSchema", sql = "select count(col1) from t2;") must beSuccessful
    }

    "apply migrations via SchemaConfig" in {
      val config = aSchemaConfig("aSchema")
        .withScripts(
          aMigrationWith("create table t1 (col1 INTEGER);"),
          aMigrationWith("create table t2 (col1 INTEGER);"))
        .withCommands(
          "create table t3 (col1 INTEGER);\n" +
            "create table t4 (col2 INTEGER)")
        .build

      val mysqld = start(anEmbeddedMysql(testConfigBuilder(v8_latest).build).addSchema(config))

      aQuery(mysqld, onSchema = "aSchema", sql = "select count(col1) from t1;") must beSuccessful
      aQuery(mysqld, onSchema = "aSchema", sql = "select count(col1) from t2;") must beSuccessful
      aQuery(mysqld, onSchema = "aSchema", sql = "select count(col1) from t3;") must beSuccessful
      aQuery(mysqld, onSchema = "aSchema", sql = "select count(col2) from t4;") must beSuccessful
    }

    "quote created table name" in {
      val config = aSchemaConfig("a-table")
        .withScripts(aMigrationWith("create table t1 (col1 INTEGER);"))
        .build

      val mysqld = start(anEmbeddedMysql(testConfigBuilder(v8_latest).build).addSchema(config))

      aQuery(mysqld, onSchema = "a-table", sql = "select count(col1) from t1;") must beSuccessful
    }

  }

  "EmbeddedMysql schema modification" should {

    "drop existing schema" in {
      val schemaConfig = aSchemaConfig("aSchema").build

      val mysqld = start(anEmbeddedMysql(testConfigBuilder(v8_latest).build).addSchema(schemaConfig))

//      mysqld must haveSchemaCharsetOf(UTF8MB4, schemaConfig.getName)

      mysqld.dropSchema(schemaConfig)

      mysqld must notHaveSchema(schemaConfig.getName)
    }

    "fail on dropping of non existing schema" in {
      val schemaConfig = aSchemaConfig("aSchema").build

      val mysqld = start(anEmbeddedMysql(testConfigBuilder(v8_latest).build))

      mysqld must notHaveSchema(schemaConfig.getName)

      mysqld.dropSchema(schemaConfig) must throwA[CommandFailedException]
    }
/*
    "add schema after mysqld start" in {
      val schemaConfig = aSchemaConfig("aSchema").build

      val mysqld = start(anEmbeddedMysql(testConfigBuilder(v8_latest).build))

      mysqld must notHaveSchema(schemaConfig.getName)

      mysqld.addSchema(schemaConfig)

      mysqld must haveSchemaCharsetOf(UTF8MB4, schemaConfig.getName)
    }
*/
    "fail on adding existing schema" in {
      val schemaConfig = aSchemaConfig("aSchema").build

      val mysqld = start(anEmbeddedMysql(testConfigBuilder(v8_latest).build).addSchema(schemaConfig))

//      mysqld must haveSchemaCharsetOf(UTF8MB4, schemaConfig.getName)

      mysqld.addSchema(schemaConfig) must throwA[CommandFailedException]
    }
  }

  "EmbeddedMysql sql execution" should {

    "Execute arbitrary scripts without dropping existing data" in {
      val schemaName = "aSchema"

      val config = aSchemaConfig(schemaName)
        .withScripts(aMigrationWith("create table t1 (col1 INTEGER);"))
        .build

      val mysqld = start(anEmbeddedMysql(testConfigBuilder(v8_latest).build))
        .addSchema(config)

      def rowCountInTable() = aSelect[java.lang.Long](mysqld, onSchema = schemaName, sql = "select count(*) from t1;")

      rowCountInTable() mustEqual 0

      mysqld.executeScripts(schemaName, classPathScript("data/arbitrary_script.sql")).asScala must contain(exactly(""))
      rowCountInTable() mustEqual 1

      mysqld.executeScripts(schemaName, classPathScript("data/arbitrary_script.sql"))
      rowCountInTable() mustEqual 2

      mysqld.executeScripts(schemaName, classPathScript("data/arbitrary_script2.sql")).asScala must haveSize(1) and contain(contain("col1"))

      rowCountInTable() mustEqual 2
    }
  }

  def pathFor(basedir: String, subdir: String): String = {
    new File(basedir, subdir).getPath
  }
}
