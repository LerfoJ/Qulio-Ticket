package net.j1407b.ticket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLiteDataSource {
    private static final Logger LOGGER = LoggerFactory.getLogger(SQLiteDataSource.class);
    private static final Connection connection;

    static {
        try {
            final File dbFile = new File("database.db");

            if (!dbFile.exists()) {
                if (dbFile.createNewFile()) {
                    LOGGER.info("Database got created.");
                } else {
                    LOGGER.info("Couldn't manifest the database.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:database.db");
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }


        try (final Statement statement = getConnection().createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS guild_settings (guild_id VARCHAR(255) PRIMARY KEY, otc VARCHAR(255), ctc VARCHAR(255), tcount VARCHAR(255) DEFAULT '0');");
            statement.execute("CREATE TABLE IF NOT EXISTS ticket_datas (guild_id VARCHAR(255), owner_id VARCHAR(255), ticket_id VARCHAR(255), supporter VARCHAR(255));");

            LOGGER.info("Table initialized.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private SQLiteDataSource() { }

    public static Connection getConnection() throws SQLException {
        return connection;
    }
}