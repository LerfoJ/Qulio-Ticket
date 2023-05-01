package net.j1407b.ticket;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.j1407b.ticket.commands.SetupTicket;
import net.j1407b.ticket.listeners.collectors.GuildCollector;
import net.j1407b.ticket.listeners.events.ClaimTicket;
import net.j1407b.ticket.listeners.events.CloseTicket;
import net.j1407b.ticket.listeners.events.CreateTicket;

import java.util.EnumSet;

public class Main extends ListenerAdapter {
    public static void main(String[] args) {
        JDABuilder noua = JDABuilder.createDefault(YamlDataSource.getConfig().getString("token"));
        noua.setEnabledIntents(EnumSet.allOf(GatewayIntent.class));
        noua.setActivity(Activity.listening("You"));
        noua.setRequestTimeoutRetry(true);
        noua.addEventListeners(new CommandsData(), new GuildCollector());
        noua.addEventListeners(new CreateTicket(), new ClaimTicket(), new CloseTicket());
        noua.addEventListeners(new SetupTicket());
        noua.build();
    }
}