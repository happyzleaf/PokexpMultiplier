package com.github.happyzleaf.pokexpmultiplier.placeholder;

import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.enums.EnumPokemon;
import com.pixelmonmod.pixelmon.storage.PixelmonStorage;
import com.pixelmonmod.pixelmon.storage.PlayerStorage;
import net.minecraft.entity.player.EntityPlayerMP;
import org.spongepowered.api.entity.living.player.Player;

public class PlaceholderHelper {
	public static EntityPixelmon getPixelmonByPos(Player player, int position) {
		PlayerStorage storage = PixelmonStorage.pokeBallManager.getPlayerStorage((EntityPlayerMP) player).orElse(null);
		return storage == null ? null : storage.getPokemon(storage.getIDFromPosition(position), ((EntityPlayerMP) player).world);
	}
	
	public static EnumPokemon getPokemon(String name) {
		return EnumPokemon.getFromNameAnyCase(name);
	}
}
