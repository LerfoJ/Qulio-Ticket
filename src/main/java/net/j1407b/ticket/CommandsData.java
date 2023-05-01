package net.j1407b.ticket;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.util.ArrayList;
import java.util.List;

public class CommandsData extends ListenerAdapter {
    @Override
    public void onReady(ReadyEvent event) {
        List<CommandData> commands = new ArrayList<>();

        OptionData otc = new OptionData(OptionType.CHANNEL, "otc", "Mention a category", true)
                .setChannelTypes(ChannelType.CATEGORY);
        OptionData ctc = new OptionData(OptionType.CHANNEL, "ctc", "Mention a category", true)
                .setChannelTypes(ChannelType.CATEGORY);


        SubcommandData ticket = new SubcommandData("ticket", "Ticket setup")
                .addOptions(otc, ctc);

        commands.add(Commands.slash("setup", "Server utility setup functions")
                .addSubcommands(ticket)
                .setGuildOnly(true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)));

        event.getJDA().updateCommands().addCommands(commands).queue();
    }
}
