package com.multiheaded.vladikbot.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author Oliver Johnson
 */
public class Database {

    public Database() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        Connection c = DriverManager.getConnection("jdbc:sqlite:./app/database/vladik.db");
        System.out.println("Opened database successfully");
    }
}
