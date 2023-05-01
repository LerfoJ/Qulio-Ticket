package net.j1407b.ticket.listeners.events;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.j1407b.ticket.SQLiteDataSource;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.EnumSet;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class CloseTicket extends ListenerAdapter {
    @Override
    public void onChannelDelete(ChannelDeleteEvent event) {
        try (PreparedStatement removeFromDB = SQLiteDataSource
                .getConnection()
                .prepareStatement("DELETE FROM ticket_datas WHERE ticket_id = ? AND guild_id = ?")) {

            removeFromDB.setString(1, event.getChannel().getId());
            removeFromDB.setString(2, event.getGuild().getId());

            removeFromDB.execute();
            removeFromDB.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String buttonId = event.getButton().getId(); assert buttonId != null;
        Member author = event.getMember(); assert author != null;

        if (buttonId.equalsIgnoreCase("close-ticket")) {
            try (PreparedStatement preparedStatement = SQLiteDataSource
                    .getConnection()
                    .prepareStatement("SELECT owner_id, ticket_id, guild_id FROM ticket_datas WHERE guild_id = ? AND ticket_id = ?")) {

                preparedStatement.setString(1, Objects.requireNonNull(event.getGuild()).getId());
                preparedStatement.setString(2, event.getChannel().getId());

                preparedStatement.execute();

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    String ticketId = resultSet.getString("ticket_id");
                    String owner_id = resultSet.getString("owner_id");

                    resultSet.close();

                    if (ticketId != null) {
                        TextChannel ticketChannel = event.getGuild().getTextChannelById(ticketId); assert ticketChannel != null;

                        try (PreparedStatement findStatement = SQLiteDataSource
                                .getConnection()
                                .prepareStatement("SELECT  guild_id, otc, ctc FROM guild_settings WHERE guild_id = ?;")){

                            findStatement.setString(1, event.getGuild().getId());

                            findStatement.execute();

                            try (ResultSet foundStatement = findStatement.executeQuery()) {
                                String otc_id = foundStatement.getString("otc");
                                String ctc_id = foundStatement.getString("ctc");

                                Category otc = event.getGuild().getCategoryById(otc_id); assert otc != null;
                                Category ctc = event.getGuild().getCategoryById(ctc_id); assert ctc != null;

                                foundStatement.close();

                                String ticketDss = ticketChannel.getName().split("-")[1];

                                ticketChannel.getManager()
                                        .setParent(ctc)
                                        .setName("closed-" + ticketDss)
                                        .putMemberPermissionOverride(Long.parseLong(owner_id), null, EnumSet.of(Permission.MESSAGE_SEND))
                                        .queueAfter(5, TimeUnit.SECONDS);

                                event.getMessage().delete().queue();

                                event.getChannel().delete().queueAfter(5, TimeUnit.DAYS);

                                try (PreparedStatement removeFromDB = SQLiteDataSource
                                        .getConnection()
                                        .prepareStatement("DELETE FROM ticket_datas WHERE ticket_id = ?")) {

                                    removeFromDB.setString(1, event.getChannel().getId());

                                    removeFromDB.execute();
                                }

                                event.deferReply(true).queue();

                                event.getHook().editOriginal("> Closing ticket...").queue();

                                ticketChannel.sendMessage("> Ticket was closed by " + author.getAsMention()).queueAfter(6, TimeUnit.SECONDS);
                            }
                        }
                    } else {
                        event.deferReply(true).queue();

                        event.getHook().editOriginal("> Couldn't close this ticket.").queue();
                    }
                }
            } catch (SQLException e) {
                Objects.requireNonNull(event.getJDA().getTextChannelById("1096419354553360465")).sendMessage(String.valueOf(e)).queue();

                event.deferReply(true).queue();

                event.getHook().editOriginal("> Couldn't close this ticket.").queue();
            }
        }
    }
}
