package net.j1407b.ticket.listeners.events;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.j1407b.ticket.SQLiteDataSource;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.EnumSet;
import java.util.Objects;

public class ClaimTicket extends ListenerAdapter {
    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String buttonId = event.getButton().getId(); assert buttonId != null;
        Member author = event.getMember(); assert author != null;

        if (buttonId.equalsIgnoreCase("claim-ticket")) {
            try (PreparedStatement checkStatement = SQLiteDataSource
                    .getConnection()
                    .prepareStatement("SELECT guild_id, owner_id, ticket_id, supporter FROM ticket_datas WHERE guild_id = ? AND ticket_id = ?;")) {

                checkStatement.setString(1, Objects.requireNonNull(event.getGuild()).getId());
                checkStatement.setString(2, event.getChannel().getId());

                checkStatement.execute();

                try (ResultSet valuesStatement = checkStatement.executeQuery()) {
                    String supporter_id_checker = valuesStatement.getString("supporter");
                    String ownerId = valuesStatement.getString("owner_id");

                    valuesStatement.close();

                    if (!author.getId().equals(ownerId)) {
                        if (supporter_id_checker == null) {
                            try (PreparedStatement savedStatement = SQLiteDataSource
                                    .getConnection()
                                    .prepareStatement("UPDATE ticket_datas SET supporter = ? WHERE ticket_id = ?;")) {

                                savedStatement.setString(1, author.getId());
                                savedStatement.setString(2, event.getChannel().getId());

                                savedStatement.execute();

                                event.deferReply(true).queue();

                                event.getHook().editOriginal("> You claimed this ticket.").queue();

                                event.getChannel().asTextChannel().getManager()
                                        .setTopic("Supporter: " + author.getAsMention())
                                        .putMemberPermissionOverride(author.getIdLong(), EnumSet.of(Permission.MESSAGE_SEND), null)
                                        .queue();
                            }
                        } else {
                            event.deferReply(true).queue();

                            event.getHook().editOriginal("> This ticket is already claimed.").queue();
                        }
                    } else {
                        event.deferReply(true).queue();

                        event.getHook().editOriginal("> You cannot claim your own ticket.").queue();
                    }
                }
            } catch (SQLException e) {
                Objects.requireNonNull(event.getJDA().getTextChannelById("1096419354553360465")).sendMessage((CharSequence) e).queue();

                event.deferReply(true).queue();

                event.getHook().editOriginal("> Couldn't claim this ticket.").queue();
            }
        }
    }
}
