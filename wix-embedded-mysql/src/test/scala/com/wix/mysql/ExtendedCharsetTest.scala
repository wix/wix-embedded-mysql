package com.wix.mysql

import com.wix.mysql.EmbeddedMysql._
import com.wix.mysql.ScriptResolver.classPathScript
import com.wix.mysql.config.Charset
import com.wix.mysql.config.MysqldConfig.SystemDefaults
import com.wix.mysql.support.IntegrationTest
import com.wix.mysql.support.IntegrationTest._

class ExtendedCharsetTest extends IntegrationTest {

  "EmbeddedMysql instance" should {

    "support non-latin characters in login/password" in {
      skipped("not really supported for windows")
//      val config = testConfigBuilder
//        .withCharset(Charset.UTF8MB4)
//        .withUser("你", "好").build
//
//      val mysqld = start(anEmbeddedMysql(config))
//
//      mysqld must beAvailableOn(3310, "你", "好", SystemDefaults.SCHEMA)
    }

    "support migration from file with extended charset" in {
      val mysqld = start(anEmbeddedMysql(testConfigBuilder().withCharset(Charset.UTF8MB4).build)
        .addSchema("aSchema", classPathScript("/db/004_update3.sql")))

      aSelect[java.lang.String](mysqld, onSchema = "aSchema", asUser="auser", sql = "select col2 from t1 where col1 = 1;") mustEqual "你好!"
    }

    "support inline migration with extended charset" in {
      val mysqld = start(anEmbeddedMysql(testConfigBuilder().withCharset(Charset.UTF8MB4).build)
        .addSchema(
          "aSchema",
          aMigrationWith("create table t1 (col1 INTEGER, col2 VARCHAR(10));\nINSERT INTO t1 values(1, '你好!');")))

      aSelect[java.lang.String](mysqld, onSchema = "aSchema", asUser="auser", sql = "select col2 from t1 where col1 = 1;") mustEqual "你好!"
    }

  }
}
