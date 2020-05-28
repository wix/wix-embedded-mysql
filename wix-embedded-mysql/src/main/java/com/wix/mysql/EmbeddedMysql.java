package com.wix.mysql;

import com.wix.mysql.config.*;
import com.wix.mysql.config.MysqldConfig.SystemDefaults;
import com.wix.mysql.distribution.Version;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import static com.wix.mysql.config.DownloadConfig.aDownloadConfig;
import static com.wix.mysql.config.MysqldConfig.SystemDefaults.SCHEMA;
import static com.wix.mysql.config.SchemaConfig.aSchemaConfig;
import static com.wix.mysql.utils.Utils.or;
import static java.lang.String.format;

public class EmbeddedMysql {
    private final static Logger logger = LoggerFactory.getLogger(EmbeddedMysql.class);
    private static final ReentrantLock localRepository = new ReentrantLock();

    protected final MysqldConfig config;
    protected final MysqldExecutable executable;
    private AtomicBoolean isRunning = new AtomicBoolean(true);

    protected EmbeddedMysql(
            final MysqldConfig mysqldConfig,
            final DownloadConfig downloadConfig) {
        logger.info("Preparing EmbeddedMysql version '{}'...", mysqldConfig.getVersion());
        this.config = mysqldConfig;
        IRuntimeConfig runtimeConfig = new RuntimeConfigBuilder().defaults(mysqldConfig, downloadConfig).build();
        MysqldStarter mysqldStarter = new MysqldStarter(runtimeConfig);

        localRepository.lock();
        try {
            this.executable = mysqldStarter.prepare(mysqldConfig);
        } finally {
            localRepository.unlock();
        }

        try {
            executable.start();
            getClient(SCHEMA, mysqldConfig.getCharset()).executeCommands(
                    format("CREATE USER '%s'@'%%' IDENTIFIED BY '%s';", mysqldConfig.getUsername(), mysqldConfig.getPassword())
                    );
            if (mysqldConfig.isGrantOption()) {
                getClient(SCHEMA, mysqldConfig.getCharset()).executeCommands(
                        format("GRANT ALL PRIVILEGES ON *.* TO '%s'@'%%' WITH GRANT OPTION", mysqldConfig.getUsername()));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public MysqldConfig getConfig() {
        return this.config;
    }

    public void reloadSchema(final String schemaName, final SqlScriptSource... scripts) {
        reloadSchema(aSchemaConfig(schemaName).withScripts(scripts).build());
    }

    public void reloadSchema(final String schemaName, final List<SqlScriptSource> scripts) {
        reloadSchema(aSchemaConfig(schemaName).withScripts(scripts).build());
    }

    public void reloadSchema(final SchemaConfig config) {
        dropSchema(config);
        addSchema(config);
    }

    public void dropSchema(final SchemaConfig schema) {
        Charset effectiveCharset = or(schema.getCharset(), config.getCharset());
        getClient(SystemDefaults.SCHEMA, effectiveCharset).executeCommands(format("DROP DATABASE %s", schema.getName()));
    }

    public List<String> executeScripts(final String schemaName, final SqlScriptSource... scripts) {
        return getClient(schemaName, config.getCharset()).executeScripts(Arrays.asList(scripts));
    }

    public List<String> executeScripts(final String schemaName, final List<SqlScriptSource> scripts) {
        return getClient(schemaName, config.getCharset()).executeScripts(scripts);
    }

    public EmbeddedMysql addSchema(final SchemaConfig schema) {
        Charset effectiveCharset = or(schema.getCharset(), config.getCharset());

        getClient(SystemDefaults.SCHEMA, effectiveCharset).executeCommands(
                format("CREATE DATABASE `%s` CHARACTER SET = %s COLLATE = %s;",
                        schema.getName(), effectiveCharset.getCharset(), effectiveCharset.getCollate()),
                format("GRANT ALL ON `%s`.* TO '%s'@'%%';", schema.getName(), config.getUsername()));

        MysqlClient client = getClient(schema.getName(), effectiveCharset);
        client.executeScripts(schema.getScripts());

        return this;
    }

    public synchronized void stop() {
        if (isRunning.getAndSet(false)) {
            executable.stop();
        }
    }

    private MysqlClient getClient(final String schemaName, final Charset charset) {
        return new MysqlClient(config, executable, schemaName, charset);
    }

    public static Builder anEmbeddedMysql(
            final Version version,
            final AdditionalConfig... additionalConfigs) {

        MysqldConfig mysqldConfig = MysqldConfig.aMysqldConfig(version).build();
        DownloadConfig downloadConfig = resolveDownloadConfig(additionalConfigs);
        return new Builder(mysqldConfig, downloadConfig);
    }

    public static Builder anEmbeddedMysql(
            final MysqldConfig mysqldConfig,
            final AdditionalConfig... additionalConfigs) {

        DownloadConfig downloadConfig = resolveDownloadConfig(additionalConfigs);
        return new Builder(mysqldConfig, downloadConfig);
    }

    private static DownloadConfig resolveDownloadConfig(AdditionalConfig[] additionalConfig) {
        AdditionalConfig first = additionalConfig.length > 0 ? additionalConfig[0] : null;

        if (first != null && first instanceof DownloadConfig) {
            return (DownloadConfig) first;
        } else {
            return aDownloadConfig().build();
        }
    }

    public static class Builder {
        private final MysqldConfig mysqldConfig;
        private DownloadConfig downloadConfig;
        private List<SchemaConfig> schemas = new ArrayList<>();

        public Builder(
                final MysqldConfig mysqldConfig,
                final DownloadConfig downloadConfig) {
            this.mysqldConfig = mysqldConfig;
            this.downloadConfig = downloadConfig;
        }

        public Builder withDownloadConfig(final DownloadConfig downloadConfig) {
            this.downloadConfig = downloadConfig;
            return this;
        }

        public Builder addSchema(final String name, final SqlScriptSource... scripts) {
            this.schemas.add(aSchemaConfig(name).withScripts(scripts).build());
            return this;
        }

        public Builder addSchema(final String name, final List<SqlScriptSource> scripts) {
            this.schemas.add(aSchemaConfig(name).withScripts(scripts).build());
            return this;
        }

        public Builder addSchema(final SchemaConfig config) {
            this.schemas.add(config);
            return this;
        }

        public EmbeddedMysql start() {
            EmbeddedMysql instance = new EmbeddedMysql(mysqldConfig, downloadConfig);

            for (SchemaConfig schema : schemas) {
                instance.addSchema(schema);
            }

            return instance;
        }
    }
}

