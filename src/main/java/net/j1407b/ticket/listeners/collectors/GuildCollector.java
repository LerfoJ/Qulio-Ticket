package net.j1407b.ticket.listeners.collectors;

import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.j1407b.ticket.SQLiteDataSource;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class GuildCollector extends ListenerAdapter {
    @Override
    public void onGuildReady(GuildReadyEvent event) {
        try (PreparedStatement insertStatement = SQLiteDataSource
                .getConnection()
                .prepareStatement("INSERT INTO guild_settings (guild_id) SELECT ? WHERE NOT EXISTS (SELECT 1 FROM guild_settings WHERE guild_id = ?);")) {

            insertStatement.setString(1, event.getGuild().getId());
            insertStatement.setString(2, event.getGuild().getId());

            insertStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        try (PreparedStatement insertStatement = SQLiteDataSource
                .getConnection()
                .prepareStatement("INSERT INTO guild_settings (guild_id) SELECT ? WHERE NOT EXISTS (SELECT 1 FROM guild_settings WHERE guild_id = ?);")) {

            insertStatement.setString(1, event.getGuild().getId());
            insertStatement.setString(2, event.getGuild().getId());

            insertStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
