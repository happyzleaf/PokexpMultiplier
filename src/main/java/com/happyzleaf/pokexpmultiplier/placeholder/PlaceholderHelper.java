package com.happyzleaf.pokexpmultiplier.placeholder;

import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import org.spongepowered.api.entity.living.player.Player;

/**
 * I made this class as a helper for some of my placeholder's scripts. But I'm pretty sure I can get rid of it now. Anyway for now I'm gonna leave this there.
 */
public class PlaceholderHelper {
	public static Pokemon getPixelmonByPos(Player player, int position) {
		return Pixelmon.storageManager.getParty(player.getUniqueId()).get(position);
	}
	
	public static EnumSpecies getPokemon(String name) {
		return EnumSpecies.getFromNameAnyCase(name);
	}
}
