package com.wix.mysql

import ch.qos.logback.classic.Level.INFO
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.classic.{Logger, LoggerContext}
import ch.qos.logback.core.read.ListAppender
import com.wix.mysql.config.{MysqldConfig, SchemaConfig}
import org.slf4j.LoggerFactory
import org.specs2.mutable.SpecWithJUnit
import org.springframework.jdbc.core.JdbcTemplate

import scala.collection.JavaConversions._
import scala.util.control.NonFatal

/**
 * @author viliusl
 * @since 27/03/15
 */
class IntegrationTest extends SpecWithJUnit {
  sequential

  val log = LoggerFactory.getLogger(this.getClass)

  def startAndVerifyDatabase(config: MysqldConfig, schema: SchemaConfig) = startAndVerify(config, schema) {
    validateConnection(config, schema)
  }

  def startAndVerify(config: MysqldConfig, schema: SchemaConfig*)(verify: => Unit) = {
    var mysqld: EmbeddedMysql = null
    try {
      mysqld = EmbeddedMysql.anEmbeddedMysql(config).start
      schema foreach { s => mysqld.addSchema(s) }
      verify
    } catch {
      case NonFatal(e) => log.error("failed in start/verification", e); throw e
    } finally mysqld.stop()
  }

  def validateConnection(config: MysqldConfig, schema: SchemaConfig) =
    new JdbcTemplate(Datasource.`with`(config, schema))
      .queryForObject("select 1;", classOf[java.lang.Long]) mustEqual 1

  def aLogFor(app: String): Iterable[String] = {
    val appender: ListAppender[ILoggingEvent] = new ListAppender[ILoggingEvent]
    val context: LoggerContext = LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]

    val logger: Logger = context.getLogger(app)

    logger.setAdditive(false)
    logger.setLevel(INFO)
    logger.detachAppender(appender.getName)
    logger.addAppender(appender)

    appender.start()

    new Iterable[String] {
      def iterator = appender.list map ( _.getMessage ) iterator
    }
  }
}