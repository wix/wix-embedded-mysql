package com.wix.mysql

import com.wix.mysql.EmbeddedMysql.anEmbeddedMysql
import com.wix.mysql.config.MysqldConfig
import com.wix.mysql.distribution.{Version, WixVersion}
import com.wix.mysql.support.IntegrationTest
import com.wix.mysql.support.IntegrationTest.testConfigBuilder
import org.specs2.matcher.Scope
import org.specs2.specification.core.Fragment

class SupportedVersionsTest extends IntegrationTest {

  val versionsToTest: Seq[WixVersion] = Seq(
    Version.v5_5_latest,
    Version.v5_6_latest,
    Version.v5_7_latest,
    Version.v8_0_11,
    Version.v8_latest) filter (_.supportsCurrentPlatform)

  trait Context extends Scope {
    val log: Iterable[String] = aLogFor("root")
  }

  Fragment.foreach(versionsToTest) { version =>
    s"$version should work on ${System.getProperty("os.name")}" in new Context {
      val config: MysqldConfig = testConfigBuilder(version).build

      val mysqld: EmbeddedMysql = start(anEmbeddedMysql(config).addSchema("aschema"))

      mysqld must beAvailableOn(config, "aschema")

      log must not(contain("Something bad happened."))
    }
  }

}
