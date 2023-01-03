package com.l1sk1sh.vladikbot;

import lombok.extern.slf4j.Slf4j;
import org.h2.tools.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

/**
 * @author l1sk1sh
 */
@Slf4j
@Component
public class H2ServerConfiguration {

    @Bean(initMethod = "start",
            destroyMethod = "shutdown")
    public Server h2Server() throws SQLException {
        log.info("Starting h2 server...");
        return Server.createTcpServer("-tcpPort", "9092", "-tcpAllowOthers");
    }
}