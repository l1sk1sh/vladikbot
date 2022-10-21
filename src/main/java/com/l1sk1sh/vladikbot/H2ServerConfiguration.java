package com.l1sk1sh.vladikbot;

import org.h2.tools.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

@Component
public class H2ServerConfiguration {

    @Bean(initMethod = "start",
            destroyMethod = "shutdown")
    public Server h2Server() throws SQLException {
        return Server.createTcpServer("-tcpPort", "9092", "-tcpAllowOthers");
    }
}