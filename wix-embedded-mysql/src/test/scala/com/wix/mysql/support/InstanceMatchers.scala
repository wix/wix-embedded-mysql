package com.wix.mysql.support

import java.util.TimeZone
import com.wix.mysql.EmbeddedMysql
import com.wix.mysql.config.Charset._
import com.wix.mysql.config.MysqldConfig.SystemDefaults
import com.wix.mysql.config.{Charset, MysqldConfig}
import com.wix.mysql.utils.Utils
import org.specs2.matcher.{Matcher, Matchers}
import scala.collection.JavaConverters.mapAsScalaMapConverter

trait InstanceMatchers extends Matchers {
  self: IntegrationTest =>

  def haveSystemVariable(name: String, aUserName: String, value: Matcher[String]): Matcher[EmbeddedMysql] =
    value ^^ { mySql: EmbeddedMysql =>
      val ds = aDataSource(mySql.getConfig, aUserName, SystemDefaults.SCHEMA)
        aSelect[String](ds, sql = s"SELECT variable_value FROM information_schema.global_variables WHERE variable_name = '$name';")
    }

  def haveCharsetOf(charset: Charset, aUserName: String): Matcher[EmbeddedMysql] =
    ===(charset) ^^ { mySql: EmbeddedMysql =>
      val ds = aDataSource(mySql.getConfig, aUserName, SystemDefaults.SCHEMA)
      aCharset(
        aSelect[String](ds, sql = "SELECT variable_value FROM information_schema.global_variables WHERE variable_name = 'character_set_server';"),
        aSelect[String](ds, sql = "SELECT variable_value FROM information_schema.global_variables WHERE variable_name = 'collation_server';"))
    }

  def haveSchemaCharsetOf(charset: Charset, aUserName: String, onSchema: String): Matcher[EmbeddedMysql] =
    ===(charset) ^^ { mySql: EmbeddedMysql =>
      val ds = aDataSource(mySql.getConfig, aUserName, onSchema)
      aCharset(
        aSelect[String](ds, sql = s"SELECT default_character_set_name FROM information_schema.SCHEMATA where SCHEMA_NAME = '$onSchema';"),
        aSelect[String](ds, sql = s"SELECT DEFAULT_COLLATION_NAME FROM information_schema.SCHEMATA where SCHEMA_NAME = '$onSchema';"))
    }

  def notHaveSchema(onSchema: String, aUserName: String): Matcher[EmbeddedMysql] =
    ===(0) ^^ { mySql: EmbeddedMysql =>
      val ds = aDataSource(mySql.getConfig, aUserName, "information_schema")
      aSelect[java.lang.Integer](ds, sql = s"SELECT COUNT(1) FROM information_schema.SCHEMATA where SCHEMA_NAME = '$onSchema';").toInt
    }

  def haveServerTimezoneMatching(timeZoneId: String, aUserName: String): Matcher[EmbeddedMysql] =
    ===(Utils.asHHmmOffset(TimeZone.getTimeZone(timeZoneId))) ^^ { mySql: EmbeddedMysql =>
      val ds = aDataSource(mySql.getConfig, aUserName, SystemDefaults.SCHEMA)
      aSelect[String](ds, sql = s"SELECT @@global.time_zone;")
    }

  def beAvailableOn(port: Int, withUser: String, password: String, andSchema: String): Matcher[EmbeddedMysql] =
    beTrue ^^ { _: EmbeddedMysql =>
      aSelect[java.lang.Long](aDataSource(withUser, password, port, andSchema), "select 1;") == 1
    }

  def beAvailableOn(config: MysqldConfig, andUserName: String, andSchema: String): Matcher[EmbeddedMysql] = {
    config.getUsers.asScala.get(andUserName) match {
      case Some(user) =>    beAvailableOn(config.getPort, user.getName, user.getPassword, andSchema)
      case _ => throw new RuntimeException(s"user: $andUserName was not defined in config")
    }
  }
}
