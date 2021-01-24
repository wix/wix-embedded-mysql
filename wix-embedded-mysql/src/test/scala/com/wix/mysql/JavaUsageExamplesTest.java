package com.wix.mysql;

import com.wix.mysql.config.DownloadConfig;
import com.wix.mysql.config.MysqldConfig;
import com.wix.mysql.config.SchemaConfig;
import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static com.wix.mysql.EmbeddedMysql.anEmbeddedMysql;
import static com.wix.mysql.ScriptResolver.classPathScript;
import static com.wix.mysql.ScriptResolver.classPathScripts;
import static com.wix.mysql.config.Charset.LATIN1;
import static com.wix.mysql.config.Charset.UTF8;
import static com.wix.mysql.config.DownloadConfig.aDownloadConfig;
import static com.wix.mysql.config.MysqldConfig.aMysqldConfig;
import static com.wix.mysql.config.ProxyFactory.aHttpProxy;
import static com.wix.mysql.config.SchemaConfig.aSchemaConfig;
import static com.wix.mysql.distribution.Version.v5_6_latest;
import static com.wix.mysql.distribution.Version.v5_7_latest;

@Ignore
public class JavaUsageExamplesTest {

    @Test
    public void defaultConfigurationAndASingleSchema() {
        EmbeddedMysql mysqld = anEmbeddedMysql(v5_6_latest)
                .addSchema("aschema", classPathScript("db/001_init.sql"))
                .start();

        //do work

        mysqld.stop(); //optional, as there is a shutdown hook
    }

    @Test
    public void defaultConfigurationAndASingleSchemaWithMultipleMigrations() {
        EmbeddedMysql mysqld = anEmbeddedMysql(v5_6_latest)
                .addSchema("aschema", classPathScripts("db/*.sql"))
                .start();

        //do work

        mysqld.stop(); //optional, as there is a shutdown hook
    }

    @Test
    public void mysqldConfigAndMultipleSchemas() {
        MysqldConfig config = aMysqldConfig(v5_7_latest)
                .withCharset(UTF8)
                .withPort(2215)
                .withUser("differentUser", "anotherPasword")
                .build();

        EmbeddedMysql mysqld = anEmbeddedMysql(config)
                .addSchema("aschema", classPathScript("db/001_init.sql"))
                .addSchema("aschema2", classPathScripts("db/*.sql"))
                .start();

        //do work

        mysqld.stop(); //optional, as there is a shutdown hook
    }

    @Test
    public void schemaConfigViaBuilder() {
        SchemaConfig schema = aSchemaConfig("aSchema")
                .withScripts(classPathScript("db/001_init.sql"))
                .withCharset(LATIN1)
                .build();

        EmbeddedMysql mysqld = anEmbeddedMysql(v5_6_latest)
                .addSchema(schema)
                .addSchema("aschema2", classPathScripts("db/*.sql"))
                .start();

        //do work

        mysqld.stop(); //optional, as there is a shutdown hook
    }

    @Test
    public void schemaReset() {
        EmbeddedMysql mysqld = anEmbeddedMysql(v5_6_latest)
                .addSchema("aschema", classPathScript("db/001_init.sql"))
                .start();

        //do work

        SchemaConfig schema = aSchemaConfig("aschema")
                .withScripts(classPathScript("db/001_init.sql"))
                .build();
        mysqld.reloadSchema(schema);

        //continue on doing work

        mysqld.stop(); //optional, as there is a shutdown hook
    }

    @Test
    public void customTimeout() {
        MysqldConfig config = aMysqldConfig(v5_6_latest)
                .withTimeout(2, TimeUnit.MINUTES)
                .build();

        EmbeddedMysql mysqld = anEmbeddedMysql(config)
                .addSchema("aschema", classPathScript("db/001_init.sql"))
                .start();

        //do work

        mysqld.stop(); //optional, as there is a shutdown hook
    }

    @Test
    public void customTempDir() {
        MysqldConfig config = aMysqldConfig(v5_6_latest)
                .withTempDir(System.getProperty("java.io.tmpdir"))
                .build();

        EmbeddedMysql mysqld = anEmbeddedMysql(config)
                .addSchema("aschema", classPathScript("db/001_init.sql"))
                .start();

        //do work

        mysqld.stop(); //optional, as there is a shutdown hook
    }

    @Test
    public void httpProxy() {
        DownloadConfig downloadConfig = aDownloadConfig()
                .withProxy(aHttpProxy("remote.host", 8080))
                .build();

        EmbeddedMysql mysqld = anEmbeddedMysql(v5_6_latest, downloadConfig)
                .addSchema("aschema", classPathScript("db/001_init.sql"))
                .start();

        //do work

        mysqld.stop(); //optional, as there is a shutdown hook
    }


}
