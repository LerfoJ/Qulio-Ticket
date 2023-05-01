package net.j1407b.ticket.listeners.events;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.j1407b.ticket.SQLiteDataSource;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.EnumSet;
import java.util.Objects;

public class CreateTicket extends ListenerAdapter {
    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String buttonId = event.getButton().getId(); assert buttonId != null;
        Member author = event.getMember(); assert author != null;

        if (buttonId.equalsIgnoreCase("create-ticket")) {
            try (PreparedStatement insertStatement = SQLiteDataSource
                    .getConnection()
                    .prepareStatement("SELECT  guild_id, owner_id, ticket_id, supporter FROM ticket_datas WHERE owner_id = ? AND guild_id = ?;")) {

                insertStatement.setString(1, author.getId());
                insertStatement.setString(2, Objects.requireNonNull(event.getGuild()).getId());

                insertStatement.execute();

                try (ResultSet resultSet = insertStatement.executeQuery()) {
                   String ownerId = resultSet.getString("owner_id");
                   String ticketId = resultSet.getString("ticket_id");

                   resultSet.close();

                   try (PreparedStatement findStatement = SQLiteDataSource
                           .getConnection()
                           .prepareStatement("SELECT  guild_id, otc, ctc, tcount FROM guild_settings WHERE guild_id = ?;")) {

                       findStatement.setString(1, event.getGuild().getId());

                       findStatement.execute();

                       try (ResultSet foundStatement = findStatement.executeQuery()) {
                           String otc_id = foundStatement.getString("otc");
                           String ctc_id = foundStatement.getString("ctc");
                           String tcount = foundStatement.getString("tcount");

                           Category otc = event.getGuild().getCategoryById(otc_id); assert otc != null;
                           Category ctc = event.getGuild().getCategoryById(ctc_id); assert ctc != null;

                           foundStatement.close();

                           if (ownerId == null) {
                               otc.createTextChannel("ticket-" + (Integer.parseInt(tcount) + 1)).queue(ticketChannel -> {
                                   try (PreparedStatement savedStatement = SQLiteDataSource
                                           .getConnection()
                                           .prepareStatement("INSERT or IGNORE INTO ticket_datas (guild_id ,owner_id, ticket_id) VALUES (?, ?, ?);")) {

                                       savedStatement.setString(1, event.getGuild().getId());
                                       savedStatement.setString(2, author.getId());
                                       savedStatement.setString(3, ticketChannel.getId());

                                       savedStatement.execute();

                                       ticketChannel.getManager()
                                               .putMemberPermissionOverride(author.getIdLong(), EnumSet.of(Permission.VIEW_CHANNEL), null)
                                               .queue();

                                       try (PreparedStatement updateStatement = SQLiteDataSource
                                               .getConnection()
                                               .prepareStatement("UPDATE guild_settings SET tcount = ? WHERE guild_id = ?;")) {

                                           updateStatement.setString(1, String.valueOf(Integer.parseInt(tcount) + 1));
                                           updateStatement.setString(2, event.getGuild().getId());

                                           updateStatement.execute();
                                       }
                                   } catch (SQLException e) {
                                       Objects.requireNonNull(event.getJDA().getTextChannelById("1096419354553360465")).sendMessage(String.valueOf(e)).queue();
                                   }
                                   try {
                                       Button closeTicket = Button.danger("close-ticket", "Close Ticket").withEmoji(event.getJDA().getEmojiById("1101243122744315904"));
                                       Button claimTicket = Button.secondary("claim-ticket", "Claim Ticket").withEmoji(event.getJDA().getEmojiById("1101243352386633878"));

                                       event.deferReply(true).queue();

                                       event.getHook().editOriginal("> Your ticket just got created " + ticketChannel.getAsMention()).queue();

                                       ticketChannel.sendMessage(
                                               author.getAsMention() + "\n" +
                                                       "> به سرویس مشتریان ایران ماینکرفت خوش اومدید.\n" +
                                                       "> لطفا صبور باشید, تیم مدیریتی بزودی با شما خواهد بود.\n" +
                                                       "> اگر درخواست پایان دادن به تیکت را دارید دکمه ی **Close Ticket** را فشار دهید").addActionRow(closeTicket, claimTicket).queue();
                                   } catch (Exception e) {
                                       Objects.requireNonNull(event.getJDA().getTextChannelById("1096419354553360465")).sendMessage(String.valueOf(e)).queue();
                                   }
                               });
                           } else {
                               event.deferReply(true).queue();

                               event.getHook().editOriginal("> You already have <#" + ticketId + ">").queue();
                           }
                       }
                   }
                }
            } catch (SQLException e) {
                Objects.requireNonNull(event.getJDA().getTextChannelById("1096419354553360465")).sendMessage(String.valueOf(e)).queue();

                event.deferReply(true).queue();

                event.getHook().editOriginal("> Couldn't create your ticket.").queue();
            }
        }
    }
}
