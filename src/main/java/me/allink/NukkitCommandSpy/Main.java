package me.allink.NukkitCommandSpy;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.*;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.SignChangeEvent;
import cn.nukkit.event.player.PlayerCommandPreprocessEvent;
import cn.nukkit.plugin.Plugin;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;

import java.util.UUID;

public class Main extends PluginBase implements CommandExecutor, Listener {
    private Config config;

    @Override
    public void onEnable() {
        config = getConfig();
        ((PluginCommand<?>)this.getCommand("commandspy")).setExecutor(this);
        this.getServer().getPluginManager().registerEvents(this, this);
    }

    private void enableCommandSpy(final Player player) {
        config.set(player.getUniqueId().toString(), true);
        saveConfig();
        reloadConfig();
        player.sendMessage("Successfully enabled CommandSpy");
    }

    private void disableCommandSpy(final Player player) {
        config.remove(player.getUniqueId().toString());
        saveConfig();
        reloadConfig();
        player.sendMessage("Successfully disabled CommandSpy");
    }

    private void broadcastToRecipients(final String string) {
        for (String uuidString : config.getKeys(false)) {
            UUID uuid = UUID.fromString(uuidString);

            if (Server.getInstance().getPlayer(uuid).isPresent()) {
                Server.getInstance().getPlayer(uuid).get().sendMessage(string);
            }
        }


    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            sender.sendMessage("Command has to be run by a player");
            return true;
        }

        final Player player = (Player) sender;

        if (args.length == 0) {
            if (config.exists(player.getUniqueId().toString())) {
                disableCommandSpy(player);
            } else {
                enableCommandSpy(player);
            }
        } else if ("on".equalsIgnoreCase(args[0])) {
            enableCommandSpy(player);
        } else if ("off".equalsIgnoreCase(args[0])) {
            disableCommandSpy(player);
        }
        return true;
    }

    @EventHandler
    void onPlayerCommandPreprocess(final PlayerCommandPreprocessEvent event) {
        if (event.isCancelled()) {
            return;
        }

        broadcastToRecipients(TextFormat.AQUA + ""
                + event.getPlayer().getName() + ""
                + TextFormat.AQUA + ": "
                + event.getMessage()
        );
    }

    @EventHandler
    void onSignChange(final SignChangeEvent event) {
        for (String uuidString : config.getKeys(false)) {
            UUID uuid = UUID.fromString(uuidString);

            broadcastToRecipients(TextFormat.AQUA + ""
                    + event.getPlayer().getName() + ""
                    + TextFormat.AQUA
                    + " created a sign with contents:"
            );

            for (String line: event.getLines()) {
                broadcastToRecipients(TextFormat.AQUA + "  " + line);
            }
        }
    }
}
