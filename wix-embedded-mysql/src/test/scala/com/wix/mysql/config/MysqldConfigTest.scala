package com.wix.mysql.config

import java.util.TimeZone
import java.util.concurrent.TimeUnit

import com.wix.mysql.config.Charset.{LATIN1, defaults}
import com.wix.mysql.config.MysqldConfig.aMysqldConfig
import com.wix.mysql.distribution.Version._
import org.specs2.mutable.SpecWithJUnit

import scala.collection.JavaConverters._

class MysqldConfigTest extends SpecWithJUnit {

  "MysqldConfig" should {

    "build with defaults" in {
      val mysqldConfig = aMysqldConfig(v5_6_latest).build()

      mysqldConfig.getPort mustEqual 3310
      mysqldConfig.getVersion mustEqual v5_6_latest
      mysqldConfig.getCharset mustEqual defaults()
      mysqldConfig.getUsers.size() mustEqual 1
      mysqldConfig.getUsers.containsKey("auser") mustEqual true
      mysqldConfig.getUsers.get("auser").getPassword mustEqual "sa"
      mysqldConfig.getUsers.get("auser").getPrivilege mustEqual Privilege.ALL
      mysqldConfig.getTimeZone mustEqual TimeZone.getTimeZone("UTC")
      mysqldConfig.getTimeout(TimeUnit.SECONDS) mustEqual 30
    }

    "accept custom port, users, charset, timezone" in {
      val mysqldConfig = aMysqldConfig(v5_6_latest)
        .withPort(1111)
        .withCharset(LATIN1)
        .withUser("otheruser", "otherpassword")
        .withUser("yetotheruser", "yetotherpassword", Privilege.SELECT)
        .withTimeZone("Europe/Vilnius")
        .withTimeout(20, TimeUnit.SECONDS)
        .build()

      mysqldConfig.getPort mustEqual 1111
      mysqldConfig.getCharset mustEqual LATIN1
      mysqldConfig.getUsers.size() mustEqual 2
      mysqldConfig.getUsers.containsKey("otheruser") mustEqual true
      mysqldConfig.getUsers.containsKey("yetotheruser") mustEqual true
      mysqldConfig.getUsers.get("otheruser").getPassword mustEqual "otherpassword"
      mysqldConfig.getUsers.get("otheruser").getPrivilege mustEqual Privilege.ALL
      mysqldConfig.getUsers.get("yetotheruser").getPassword mustEqual "yetotherpassword"
      mysqldConfig.getUsers.get("yetotheruser").getPrivilege mustEqual Privilege.SELECT
      mysqldConfig.getTimeZone mustEqual TimeZone.getTimeZone("Europe/Vilnius")
      mysqldConfig.getTimeout(TimeUnit.MILLISECONDS) mustEqual 20000
    }

    "accept custom system variables" in {
      val mysqldConfig = aMysqldConfig(v5_6_latest)
        .withServerVariable("some-int", 123)
        .withServerVariable("some-string", "one")
        .withServerVariable("some-boolean", false)
        .build

      mysqldConfig.getServerVariables.asScala.map(_.toCommandLineArgument) mustEqual
        Seq("--some-int=123", "--some-string=one", "--some-boolean=false")
    }

    "accept free port" in {
      val mysqldConfig = aMysqldConfig(v5_6_latest)
        .withFreePort()
        .build()

      mysqldConfig.getPort mustNotEqual 3310
    }

    "fail if building with user 'root'" in {
      aMysqldConfig(v5_6_latest)
        .withUser("root", "doesnotmatter")
        .build() must throwA[IllegalArgumentException](message = "Usage of username 'root' is forbidden")
    }

    "fail if building with v8 privileges for v5 mysql" in {
      aMysqldConfig(v5_5_52)
        .withUser("auser", "doesnotmatter", Privilege.CREATE_ROLE)
        .build() must throwA[IllegalArgumentException](message = "Privilege CREATE_ROLE not compatible with Version 5.5.52")
      aMysqldConfig(v5_6_36)
        .withUser("auser", "doesnotmatter", Privilege.CREATE_ROLE)
        .build() must throwA[IllegalArgumentException](message = "Privilege CREATE_ROLE not compatible with Version 5.6.36")
      aMysqldConfig(v5_7_27)
        .withUser("auser", "doesnotmatter", Privilege.CREATE_ROLE)
        .build() must throwA[IllegalArgumentException](message = "Privilege CREATE_ROLE not compatible with Version 5.7.27")
    }

  }
}
