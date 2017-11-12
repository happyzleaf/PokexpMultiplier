package com.github.happyzleaf.pokexpmultiplier.placeholder;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;

public class PlaceholderUtility {
	private static boolean canLoad = false;
	
	public static void init() {
		if (Sponge.getPluginManager().isLoaded("placeholderapi")) {
			canLoad = true;
			PlaceholderGetter.init();
		}
	}
	
	public static String replaceIfAvailable(String algorithm, Player player) {
		return canLoad ? PlaceholderGetter.INSTANCE.replace(algorithm, player) : algorithm;
	}
}
