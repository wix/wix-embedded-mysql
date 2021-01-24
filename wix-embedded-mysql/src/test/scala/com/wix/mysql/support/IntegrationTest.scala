package com.wix.mysql.support

import java.util.UUID
import java.util.concurrent.TimeUnit
import ch.qos.logback.classic.Level.INFO
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.classic.{Logger, LoggerContext}
import ch.qos.logback.core.read.ListAppender
import com.wix.mysql.config.MysqldConfig
import com.wix.mysql.distribution.{Version, WixVersion}
import com.wix.mysql.{EmbeddedMysql, Sources, SqlScriptSource}
import de.flapdoodle.embed.process.io.directories.UserHome
import javax.sql.DataSource
import org.apache.commons.dbcp2.BasicDataSource
import org.apache.commons.io.FileUtils._
import org.slf4j
import org.slf4j.LoggerFactory
import org.slf4j.LoggerFactory.getLogger
import org.specs2.mutable.SpecWithJUnit
import org.specs2.specification.BeforeAfterEach
import org.springframework.dao.DataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import scala.collection.JavaConversions._
import scala.reflect._

abstract class IntegrationTest extends SpecWithJUnit with BeforeAfterEach
  with InstanceMatchers with TestResourceSupport with JdbcSupport {

  sequential

  var mysqldInstances: Seq[EmbeddedMysql] = Seq()
  val log: slf4j.Logger = getLogger(this.getClass)

  def before: Any = mysqldInstances = Seq()

  def after: Any = mysqldInstances.foreach(_.stop)

  def start(mysqld: EmbeddedMysql.Builder): EmbeddedMysql = {
    val instance = mysqld.start
    mysqldInstances = mysqldInstances :+ instance
    instance
  }


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
      def iterator = appender.list map (_.getMessage) iterator
    }
  }

  def withCleanRepo[T](f: => T): T = {
    val repository = new UserHome(".embedmysql").asFile
    val backupFolder = new UserHome(s".embedmysql-${UUID.randomUUID()}").asFile

    if (!repository.exists()) {
      f
    } else {
      moveDirectory(repository, backupFolder)
      try {
        f
      } finally {
        deleteDirectory(repository)
        moveDirectory(backupFolder, repository)
      }
    }
  }
}

object IntegrationTest {
  val targetTestVersion: WixVersion = Version.v5_7_latest

  def testConfigBuilder(version: WixVersion = targetTestVersion): MysqldConfig.Builder = MysqldConfig
    .aMysqldConfig(version)
    .withTimeout(60, TimeUnit.SECONDS)
}

trait JdbcSupport {
  self: IntegrationTest =>

  def aDataSource(config: MysqldConfig, schema: String): DataSource =
    aDataSource(config.getUsername, config.getPassword, config.getPort, schema)

  def aDataSource(user: String, password: String, port: Int, schema: String): DataSource = {
    val dataSource: BasicDataSource = new BasicDataSource
    dataSource.setDriverClassName("com.mysql.jdbc.Driver")
    dataSource.setUrl(s"jdbc:mysql://localhost:$port/$schema?useSSL=false")
    dataSource.setUsername(user)
    dataSource.setPassword(password)
    dataSource
  }

  def aJdbcTemplate(mysqld: EmbeddedMysql, forSchema: String): JdbcTemplate =
    new JdbcTemplate(aDataSource(mysqld.getConfig, forSchema))

  def aSelect[T: ClassTag](ds: DataSource, sql: String): T =
    new JdbcTemplate(ds).queryForObject(sql, classTag[T].runtimeClass.asInstanceOf[Class[T]])

  def aSelect[T: ClassTag](mysqld: EmbeddedMysql, onSchema: String, sql: String): T =
    aJdbcTemplate(mysqld, onSchema).queryForObject(sql, classTag[T].runtimeClass.asInstanceOf[Class[T]])

  def aQuery(mysqld: EmbeddedMysql, onSchema: String, sql: String): Unit =
    aJdbcTemplate(mysqld, forSchema = onSchema).execute(sql)

  def anUpdate(mysqld: EmbeddedMysql, onSchema: String, sql: String): Unit =
    aJdbcTemplate(mysqld, forSchema = onSchema).execute(sql)

  def aMigrationWith(sql: String): SqlScriptSource = Sources.fromFile(createTempFile(sql))

  def beSuccessful = not(throwAn[Exception])

  def failWith(fragment: String) = throwA[DataAccessException].like { case e =>
    e.getMessage must contain(fragment)
  }

}
