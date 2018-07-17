package com.happyzleaf.pokexpmultiplier;

import com.happyzleaf.pokexpmultiplier.placeholder.PlaceholderUtility;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.entity.living.player.Player;

import java.util.Optional;

/***************************************
 * PokexpMultiplier
 * Created by happyzleaf on 21/03/2017.
 *
 * Copyright (c). All rights reserved.
 ***************************************/
public class AlgorithmUtilities {
	//Returns the algorithm's name
	public static String algorithmPerUser(Player player) {
		Optional<String> algorithm = player.getContainingCollection().getSubject(player.getIdentifier()).get().getOption("pokexp_algorithm");
		return algorithm.isPresent() ? algorithm.get() : PokexpConfig.getInstance().getConfig().getNode("config", "default_algorithm").getString();
	}
	
	//Needs the algorithm's name
	public static String valuePerUser(Player player, String algorithmName) {
		Optional<String> value = player.getContainingCollection().getSubject(player.getIdentifier()).get().getOption("pokexp_value");
		return value.isPresent() ? value.get() : PokexpConfig.getInstance().getConfig().getNode("algorithms", algorithmName, "default_value").getString();
	}
	
	//Needs the algorithm's name and returns the actual algorithm
	public static String parseAlgorithmWithValues(Player player, String algorithmName, int startingExp, int partyPosition, String pokemonName) {
		return PlaceholderUtility.replaceIfAvailable(PokexpConfig.getInstance().getConfig().getNode("algorithms", algorithmName, "algorithm").getString()
					.replace("#VALUE", valuePerUser(player, algorithmName))
					.replace("#POKEMON-EXP", "" + startingExp)
					.replace("#POKEMON", pokemonName)
					.replace("#PARTY-POSITION", "" + partyPosition)
				, player);
	}
	
	public static String parseInfoWithValues(Player player, String algorithmName) {
		ConfigurationNode node = PokexpConfig.getInstance().getConfig().getNode("algorithms", algorithmName, "messages", "info");
		return PlaceholderUtility.replaceIfAvailable((node.isVirtual() ? PokexpConfig.getInstance().getConfig().getNode("config", "default_info").getString() : node.getString())
					.replace("#PLAYER", player.getName())
					.replace("#VALUE", valuePerUser(player, algorithmName))
				, player);
	}
	
	//Thanks to Boann! (http://stackoverflow.com/users/964243/boann)
	public static double eval(final String str) {
		return new Object() {
			int pos = -1, ch;
			
			void nextChar() {
				ch = (++pos < str.length()) ? str.charAt(pos) : -1;
			}
			
			boolean eat(int charToEat) {
				while (ch == ' ') nextChar();
				if (ch == charToEat) {
					nextChar();
					return true;
				}
				return false;
			}
			
			double parse() {
				nextChar();
				double x = parseExpression();
				if (pos < str.length()) throw new RuntimeException("Unexpected: " + (char)ch);
				return x;
			}
			
			// Grammar:
			// expression = term | expression `+` term | expression `-` term
			// term = factor | term `*` factor | term `/` factor
			// factor = `+` factor | `-` factor | `(` expression `)`
			//        | number | functionName factor | factor `^` factor
			
			double parseExpression() {
				double x = parseTerm();
				for (;;) {
					if      (eat('+')) x += parseTerm(); // addition
					else if (eat('-')) x -= parseTerm(); // subtraction
					else return x;
				}
			}
			
			double parseTerm() {
				double x = parseFactor();
				for (;;) {
					if      (eat('*')) x *= parseFactor(); // multiplication
					else if (eat('/')) x /= parseFactor(); // division
					else return x;
				}
			}
			
			double parseFactor() {
				if (eat('+')) return parseFactor(); // unary plus
				if (eat('-')) return -parseFactor(); // unary minus
				
				double x;
				int startPos = this.pos;
				if (eat('(')) { // parentheses
					x = parseExpression();
					eat(')');
				} else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
					while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
					x = Double.parseDouble(str.substring(startPos, this.pos));
				} else if (ch >= 'a' && ch <= 'z') { // functions
					while (ch >= 'a' && ch <= 'z') nextChar();
					String func = str.substring(startPos, this.pos);
					x = parseFactor();
					if (func.equals("sqrt")) x = Math.sqrt(x);
					else if (func.equals("sin")) x = Math.sin(Math.toRadians(x));
					else if (func.equals("cos")) x = Math.cos(Math.toRadians(x));
					else if (func.equals("tan")) x = Math.tan(Math.toRadians(x));
					else throw new RuntimeException("Unknown function: " + func);
				} else {
					throw new RuntimeException("Unexpected: " + (char)ch);
				}
				
				if (eat('^')) x = Math.pow(x, parseFactor()); // exponentiation
				
				return x;
			}
		}.parse();
	}
}
