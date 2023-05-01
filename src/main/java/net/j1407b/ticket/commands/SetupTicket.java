package net.j1407b.ticket.commands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.j1407b.ticket.SQLiteDataSource;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class SetupTicket extends ListenerAdapter {
    private final Permission requiredPermission = Permission.ADMINISTRATOR;

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        String modalId = event.getModalId();

        if (modalId.equalsIgnoreCase("ticket-text-modal")) {
            String ticketMainMessage = Objects.requireNonNull(event.getValue("ticket-text")).getAsString();

            Button createTicket = Button.success("create-ticket", "Create Ticket").withEmoji(event.getJDA().getEmojiById("1101177456653774960"));

            event.getChannel().sendMessage(ticketMainMessage).addActionRow(createTicket).queue();

            event.deferReply(true).queue();

            event.getHook().editOriginal("> Ticket setup was completed.").queue();
        }
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String commandName = event.getName();
        String subcommandName = event.getSubcommandName(); assert subcommandName != null;

        if (commandName.equalsIgnoreCase("setup")) {
            if (subcommandName.equalsIgnoreCase("ticket")) {
                OptionMapping otcOption = event.getOption("otc"); assert otcOption != null;
                Category otc = otcOption.getAsChannel().asCategory();

                OptionMapping ctcOption = event.getOption("ctc"); assert ctcOption != null;
                Category ctc = ctcOption.getAsChannel().asCategory();

                try (PreparedStatement insertStatement = SQLiteDataSource
                        .getConnection()
                        .prepareStatement("UPDATE guild_settings SET otc = ?, ctc = ? WHERE guild_id = ?;")){

                    insertStatement.setString(1, otc.getId());
                    insertStatement.setString(2, ctc.getId());
                    insertStatement.setString(3, Objects.requireNonNull(event.getGuild()).getId());

                    insertStatement.execute();

                    TextInput ticketStartingText = TextInput.create("ticket-text", "Ticket Text", TextInputStyle.PARAGRAPH)
                            .setMinLength(1)
                            .setRequired(true)
                            .setPlaceholder("> Welcome to " + event.getGuild().getName() + ". \n" +
                                    "> if you want to contact server support pres **Create Ticket**.")
                            .build();

                    Modal replyingModal  = Modal.create("ticket-text-modal", "Enter your main ticket message")
                            .addActionRow(ticketStartingText)
                            .build();

                    event.replyModal(replyingModal).queue();
                } catch (SQLException e) {
                    Objects.requireNonNull(event.getJDA().getTextChannelById("1096419354553360465")).sendMessage(String.valueOf(e)).queue();

                    event.deferReply(true).queue();

                    event.getHook().editOriginal("> Ticket setup failed.").queue();
                }
            }
        }
    }

    @Override    public void onMessageReceived(MessageReceivedEvent event) {
        String[] args = event.getMessage().getContentRaw().split(" ");
        Member author = event.getMember(); assert author != null;

        if (args[0].equalsIgnoreCase("q!setup")) {
            if (args.length > 1) {
                if (args.length > 3) {
                    if (args[1].equalsIgnoreCase("ticket")) {
                        if (author.hasPermission(requiredPermission)) {
                            String otcId = null;
                            String ctcId = null;

                            try {
                                otcId = args[2].replace("<#", "").replace(">", "");

                                event.getGuild().getCategoryById(otcId);
                            } catch (Exception e) {
                                event.getChannel().sendMessage("> Provide a true category.").queue(notification -> {
                                    if (notification != null) {
                                        notification.delete().queueAfter(10, TimeUnit.SECONDS);
                                    }
                                });
                            }

                            try {
                                ctcId = args[3].replace("<#", "").replace(">", "");

                                event.getGuild().getCategoryById(ctcId);
                            } catch (Exception e) {
                                event.getChannel().sendMessage("> Provide a true category.").queue(notification -> {
                                    if (notification != null) {
                                        notification.delete().queueAfter(10, TimeUnit.SECONDS);
                                    }
                                });
                            }

                            try (PreparedStatement insertStatement = SQLiteDataSource
                                    .getConnection()
                                    .prepareStatement("UPDATE guild_settings SET otc = ?, ctc = ? WHERE guild_id = ?;")){

                                insertStatement.setString(1, otcId);
                                insertStatement.setString(2, ctcId);
                                insertStatement.setString(3, event.getGuild().getId());

                                insertStatement.execute();

                                Button createTicket = Button.success("create-ticket", "Create Ticket").withEmoji(event.getJDA().getEmojiById("1101177456653774960"));

                                event.getChannel().sendMessage("> Welcome to " + event.getGuild().getName() + ". \n To create a ticket press ***Create Ticket**.").addActionRow(createTicket).queue();

                                event.getChannel().sendMessage("> Ticket setup was completed.").queue(notification -> {
                                    if (notification != null) {
                                        notification.delete().queueAfter(10, TimeUnit.SECONDS);
                                    }
                                });
                            } catch (SQLException e) {
                                Objects.requireNonNull(event.getJDA().getTextChannelById("1096419354553360465")).sendMessage(String.valueOf(e)).queue();

                                event.getChannel().sendMessage("> Ticket setup failed.").queue(notification -> {
                                    if (notification != null) {
                                        notification.delete().queueAfter(10, TimeUnit.SECONDS);
                                    }
                                });
                            }

                            event.getMessage().delete().queue();
                        } else {
                            event.getChannel().sendMessage("> You don't have enough permission(s).\n > Required permission(s): `ADMINISTRATOR`").queue(notification -> {
                                if (notification != null) {
                                    notification.delete().queueAfter(10, TimeUnit.SECONDS);
                                }
                            });
                        }
                    }
                } else {
                    event.getChannel().sendMessage("> Incorrect syntax.\n > Try: `q!setup ticket [otc] [ctc]`").queue(notification -> {
                        if (notification != null) {
                            notification.delete().queueAfter(10, TimeUnit.SECONDS);
                        }
                    });
                }
            } else {
                event.getChannel().sendMessage("> Incorrect syntax.\n > Try: `q!setup [function]...`").queue(notification -> {
                    if (notification != null) {
                        notification.delete().queueAfter(10, TimeUnit.SECONDS);
                    }
                });
            }
        }
    }
}
