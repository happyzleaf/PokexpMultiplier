package com.happyzleaf.pokexpmultiplier;

import com.happyzleaf.pokexpmultiplier.placeholder.PlaceholderUtility;
import com.pixelmonmod.pixelmon.api.enums.ExperienceGainType;
import com.pixelmonmod.pixelmon.api.events.ExperienceGainEvent;
import com.pixelmonmod.pixelmon.config.PixelmonConfig;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.serializer.TextSerializers;

/**
 * @author happyzleaf
 * @since 31/05/2019
 */
public class EventHandler {
	@SubscribeEvent
	public static void onExperienceGain(ExperienceGainEvent event) {
		if (event.getType() == ExperienceGainType.BATTLE) {
			try {
				//This is just for the message, there are no problems to multiply the exp of a max leveled pokemon
				if (event.pokemon.isEgg() || event.pokemon.getLevel() == PixelmonConfig.maxLevel) return;
				
				Player player = (Player) event.pokemon.getPlayerOwner();
				if (player.hasPermission(PokexpMultiplier.PLUGIN_ID + ".enable")) {
					int oldExp = event.getExperience();
					String algorithm = AlgorithmUtilities.algorithmPerUser(player);
					int result = (int) AlgorithmUtilities.eval(AlgorithmUtilities.parseAlgorithmWithValues(player, algorithm, oldExp, event.pokemon.getPartyPosition(), event.pokemon.getBaseStats().pixelmonName));
					event.setExperience(result);
					ConfigurationNode message = PokexpConfig.getInstance().getConfig().getNode("algorithms", algorithm, "messages", "message");
					if (!message.isVirtual()) {
						player.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(
								PlaceholderUtility.replaceIfAvailable(message.getString()
												.replace("#POKEMON", event.pokemon.getNickname())
												.replace("#PLAYER", player.getName())
												.replace("#PARTY-POSITION", "" + event.pokemon.getPartyPosition())
												.replace("#OLD-EXP", "" + oldExp)
												.replace("#NEW-EXP", "" + event.getExperience())
												.replace("#VALUE", "" + AlgorithmUtilities.valuePerUser(player, algorithm))
										, player)
						));
					}
				}
			} catch (Exception e) {
				PokexpMultiplier.LOGGER.error("PokexpMultiplier has thrown an exception!", e);
			}
		}
	}
}
