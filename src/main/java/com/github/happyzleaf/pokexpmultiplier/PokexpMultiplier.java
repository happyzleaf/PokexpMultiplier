package com.github.happyzleaf.pokexpmultiplier;

import com.google.inject.Inject;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.events.ExperienceGainEvent;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.io.File;

@Plugin(id = "pokexpmultiplier", name = "ExpMultiplier", version = "1.0.0", authors = {"happyzlife"}, dependencies = {@Dependency(id = "pixelmon")})
public class PokexpMultiplier {
    public static final String PLUGIN_ID = "pokexpmultiplier";

    @Inject
    private Logger logger;

    @Inject
    @DefaultConfig(sharedRoot = true)
    private File configFile;

    @Inject
    @DefaultConfig(sharedRoot = true)
    ConfigurationLoader<CommentedConfigurationNode> configLoader;

    @Listener
    public void onGameInitialization(GameInitializationEvent event) {
        PokexpConfig.getInstance().setup(configFile, configLoader);
        Pixelmon.EVENT_BUS.register(this);

        CommandSpec reload = CommandSpec.builder()
                .executor(new CommandExecutor() {
                    @Override
                    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
                        PokexpConfig.getInstance().loadConfig();
                        src.sendMessage(Text.of(TextColors.DARK_GREEN, "[PokexpMultiplier] Config(s) reloaded!"));
                        return CommandResult.success();
                    }
                })
                .description(Text.of("Reload configs."))
                .permission(PLUGIN_ID + ".reload")
                .build();
        CommandSpec dfault = CommandSpec.builder()
                .arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("multiplier"))))
                .executor(new CommandExecutor() {
                    @Override
                    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
                        try {
                            String multiplier = (String) args.getOne("multiplier").get();
                            PokexpConfig.getInstance().getConfig().getNode("multiplier", "default").setValue(Float.parseFloat(multiplier));
                            PokexpConfig.getInstance().saveAndLoad();
                            src.sendMessage(Text.of(TextColors.DARK_GREEN, "[PokexpMultiplier] The exp multiplier of " + multiplier + " has been succesfully setted as default."));
                            return CommandResult.success();
                        } catch(NumberFormatException e) {
                            src.sendMessage(Text.of(TextColors.RED, "[PokexpMultiplier] The multiplier MUST be a number!"));
                            return CommandResult.successCount(0);
                        }
                    }
                })
                .description(Text.of("Set the default experience multiplier."))
                .permission(PLUGIN_ID + ".default")
                .build();
        CommandSpec set = CommandSpec.builder()
                .arguments(GenericArguments.onlyOne(GenericArguments.player(Text.of("player"))), GenericArguments.onlyOne(GenericArguments.string(Text.of("multiplier"))))
                .executor(new CommandExecutor() {
                    @Override
                    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
                        Player player = (Player) args.getOne("player").get();
                        try {
                            String multiplier = (String) args.getOne("multiplier").get();
                            PokexpConfig.getInstance().getConfig().getNode("multiplier", "users", player.getUniqueId().toString()).setValue(Float.parseFloat(multiplier));
                            PokexpConfig.getInstance().saveAndLoad();
                            src.sendMessage(Text.of(TextColors.DARK_GREEN, "[PokexpMultiplier] The exp multiplier of " + multiplier + " has been succesfully setted to " + player.getName() + "."));
                            return CommandResult.success();
                        } catch(NumberFormatException e) {
                            src.sendMessage(Text.of(TextColors.RED, "[PokexpMultiplier] The multiplier MUST be a number!"));
                            return CommandResult.successCount(0);
                        }
                    }
                })
                .description(Text.of("Set a custom experience multiplier per player."))
                .permission(PLUGIN_ID + ".player.set")
                .build();
        CommandSpec remove = CommandSpec.builder()
                .arguments(GenericArguments.onlyOne(GenericArguments.player(Text.of("player"))))
                .executor(new CommandExecutor() {
                    @Override
                    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
                        Player player = (Player) args.getOne("player").get();
                        if(!PokexpConfig.getInstance().getConfig().getNode("multiplier", "users", player.getUniqueId().toString()).isVirtual()) {
                            PokexpConfig.getInstance().getConfig().getNode("multiplier", "users").removeChild(player.getUniqueId().toString());
                            PokexpConfig.getInstance().saveAndLoad();
                            src.sendMessage(Text.of(TextColors.DARK_GREEN, "[PokexpMultiplier] Succesfully removed " + player.getName() + " from the config."));
                            return CommandResult.success();
                        } else {
                            src.sendMessage(Text.of(TextColors.RED, "[PokexpMultiplier] " + player.getName() + "doesn't have any custom multiplier."));
                            return CommandResult.successCount(0);
                        }
                    }
                })
                .description(Text.of("Remove a custom experience multiplier from a player."))
                .permission(PLUGIN_ID + ".player.remove")
                .build();
        CommandSpec player = CommandSpec.builder()
                .child(set, "set")
                .child(remove, "remove")
                .build();
        CommandSpec pokexp = CommandSpec.builder()
                .child(reload, "reload")
                .child(dfault, "default")
                .child(player, "player")
                .build();
        Sponge.getGame().getCommandManager().register(this, pokexp, "pokexp", "pkexp");

        logger.info("Loaded!");
    }

    @SubscribeEvent
    public void onExperienceGain(ExperienceGainEvent event) {
        Player player = (Player) event.pokemon.getOwner();
        if(player.hasPermission(PLUGIN_ID + ".enable")) {
            int oldExp = event.experience;

            event.experience *= quantityPerUser(player);

            if(PokexpConfig.getInstance().getConfig().getNode("message", "enable").getBoolean())
                player.sendMessage(Text.of(TextColors.DARK_GREEN, PokexpConfig.getInstance().getConfig().getNode("message", "message").getString()
                        .replaceAll("#POKEMON", event.pokemon.getName())
                        .replaceAll("#PLAYER", player.getName())
                        .replaceAll("#OLD_EXP", "" + oldExp)
                        .replaceAll("#NEW_EXP", "" + event.experience)
                        .replaceAll("#MULTIPLIER", "" + quantityPerUser(player))
                ));
        }
    }

    private float quantityPerUser(Player player) {
        String expQuantity = player.getContainingCollection().get(player.getIdentifier()).getOption("pokexp").orElse("");

        logger.info(player.getUniqueId().toString());
        if(!PokexpConfig.getInstance().getConfig().getNode("multiplier", "users", player.getUniqueId().toString()).isVirtual()) {
            return PokexpConfig.getInstance().getConfig().getNode("multiplier", "users", player.getUniqueId().toString()).getFloat();
        } else if(expQuantity != null && !expQuantity.isEmpty())
            return Float.parseFloat(expQuantity);
        else
            return PokexpConfig.getInstance().getConfig().getNode("multiplier", "default").getFloat();
    }
}
