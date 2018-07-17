package com.happyzleaf.pokexpmultiplier.placeholder;

import com.happyzleaf.pokexpmultiplier.PokexpMultiplier;
import me.rojo8399.placeholderapi.PlaceholderService;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.service.ChangeServiceProviderEvent;

public class PlaceholderGetter {
	private PlaceholderGetter() {
	}
	
	static PlaceholderGetter INSTANCE = new PlaceholderGetter();
	
	static void init() {
		Sponge.getEventManager().registerListeners(PokexpMultiplier.instance, INSTANCE);
		INSTANCE.papi = Sponge.getServiceManager().provideUnchecked(PlaceholderService.class);
	}
	
	private PlaceholderService papi;
	
	@Listener
	public void onChangeServiceProvider(ChangeServiceProviderEvent event) {
		if (event.getService().equals(PlaceholderService.class)) {
			papi = (PlaceholderService) event.getNewProviderRegistration().getProvider();
		}
	}
	
	String replace(String algorithm, Player player) {
		return papi.replacePlaceholders(algorithm, player, null).toPlain();
	}
}
