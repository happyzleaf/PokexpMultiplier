package com.happyzleaf.pokexpmultiplier;

import com.google.inject.Inject;
import com.happyzleaf.pokexpmultiplier.placeholder.PlaceholderUtility;
import com.pixelmonmod.pixelmon.Pixelmon;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.io.File;

@Plugin(id = PokexpMultiplier.PLUGIN_ID, name = PokexpMultiplier.PLUGIN_NAME, version = PokexpMultiplier.VERSION,
		authors = {"happyzlife"}, url = "https://www.happyzleaf.com/",
		dependencies = {
				@Dependency(id = "pixelmon"),
				@Dependency(id = "placeholderapi", version = "[4.4,)", optional = true)
		})
public class PokexpMultiplier {
	public static final String PLUGIN_ID = "pokexpmultiplier";
	public static final String PLUGIN_NAME = "PokexpMultiplier";
	public static final String VERSION = "1.1.10";

	public static final Logger LOGGER = LoggerFactory.getLogger(PLUGIN_NAME);

	public static PokexpMultiplier instance;

	@Inject
	@DefaultConfig(sharedRoot = true)
	private File configFile;

	@Inject
	@DefaultConfig(sharedRoot = true)
	ConfigurationLoader<CommentedConfigurationNode> configLoader;

	@Listener
	public void preInit(GamePreInitializationEvent event) {
		instance = this;
	}

	@Listener
	public void init(GameInitializationEvent event) {
		PlaceholderUtility.init();
		PokexpConfig.getInstance().setup(configFile, configLoader);
		Pixelmon.EVENT_BUS.register(EventHandler.class);

		CommandSpec info = CommandSpec.builder()
				.arguments(GenericArguments.optional(GenericArguments.requiringPermission(GenericArguments.onlyOne(GenericArguments.player(Text.of("player"))), PLUGIN_ID + ".info.others")))
				.executor((src, args) -> {
					if (args.hasAny("player")) {
						Player player = (Player) args.getOne("player").get();
						src.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(AlgorithmUtilities.parseInfoWithValues(player, AlgorithmUtilities.algorithmPerUser(player))));
						return CommandResult.success();
					} else {
						if (src instanceof Player) {
							src.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(AlgorithmUtilities.parseInfoWithValues((Player) src, AlgorithmUtilities.algorithmPerUser((Player) src))));
							return CommandResult.success();
						} else {
							src.sendMessage(Text.of(TextColors.DARK_RED, "[" + PLUGIN_NAME + "]", TextColors.RED, " You MUST be in-game in order to execute this command."));
							return CommandResult.successCount(0);
						}
					}
				})
				.permission(PLUGIN_ID + ".info.me")
				.description(Text.of("Get the player's experience algorithm info."))
				.build();
		CommandSpec pokexp = CommandSpec.builder()
				.child(info, "info")
				.build();
		Sponge.getGame().getCommandManager().register(this, pokexp, "pokexp", "pkexp");

		LOGGER.info("{} v{} Loaded! This plugin was made by happyzleaf. (https://happyzleaf.com/)", PLUGIN_NAME, VERSION);
	}

	@Listener
	public void onReload(GameReloadEvent event) {
		PokexpConfig.getInstance().loadConfig();
		if (event instanceof MessageReceiver) {
			((MessageReceiver) event.getSource()).sendMessage(Text.of(TextColors.DARK_GREEN, "[" + PLUGIN_NAME + "]", TextColors.GREEN, " Configs reloaded!"));
		} else {
			LOGGER.info("[" + PLUGIN_NAME + "] Configs reloaded!");
		}
	}
}
