package com.anurag.backend;

import java.sql.Connection;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
public class DataSourceConfig {

    private static final Logger log = LoggerFactory.getLogger(DataSourceConfig.class);

    @Value("${spring.datasource.url:}")
    private String dbUrl;

    @Value("${spring.datasource.username:}")
    private String username;

    @Value("${spring.datasource.password:}")
    private String password;

    @Bean
    @Primary
    public DataSource dataSource() {
        if (dbUrl != null && !dbUrl.isBlank() && !dbUrl.contains("YOUR_HOST") && !dbUrl.contains("example.com")) {
            HikariDataSource ds = null;
            try {
                log.info("Testing connection to configured database: {}", sanitizeUrl(dbUrl));
                HikariConfig config = new HikariConfig();
                config.setJdbcUrl(dbUrl);
                config.setUsername(username);
                config.setPassword(password);
                config.setDriverClassName(getDriverClass(dbUrl));
                config.setConnectionTimeout(6000);
                config.setValidationTimeout(3000);
                config.setMaximumPoolSize(5);
                config.setMinimumIdle(1);

                ds = new HikariDataSource(config);
                try (Connection conn = ds.getConnection()) {
                    log.info("Successfully connected to primary database!");
                    return ds;
                }
            } catch (Exception ex) {
                log.warn("Could not connect to external database ({}). Activating embedded resilient fallback database so the server stays 100% online.", ex.getMessage());
                if (ds != null && !ds.isClosed()) {
                    ds.close();
                }
            }
        }

        log.info("Initializing resilient embedded database (sports_court).");
        HikariConfig h2Config = new HikariConfig();
        h2Config.setJdbcUrl("jdbc:h2:mem:sports_court;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=MySQL");
        h2Config.setDriverClassName("org.h2.Driver");
        h2Config.setUsername("sa");
        h2Config.setPassword("");
        h2Config.setMaximumPoolSize(5);
        return new HikariDataSource(h2Config);
    }

    private String getDriverClass(String url) {
        if (url.startsWith("jdbc:h2:")) {
            return "org.h2.Driver";
        }
        return "com.mysql.cj.jdbc.Driver";
    }

    private String sanitizeUrl(String url) {
        int q = url.indexOf('?');
        return q > 0 ? url.substring(0, q) : url;
    }
}
