package com.mbe.addons.ui.ux.placeholders;

import org.bukkit.entity.Player;

import com.darkbladedev.engine.api.logging.EngineLogger;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class PlaceholderProcessor {
    private static final Set<String> warnedInvalid = ConcurrentHashMap.newKeySet();
    private static final Set<String> warnedUnresolved = ConcurrentHashMap.newKeySet();

    private PlaceholderProcessor() {
    }

    public static String process(EngineLogger logger, Player player, String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        String normalized = PlaceholderSyntax.normalizeToDollar(input);

        for (String token : PlaceholderSyntax.extractDollarTokens(normalized)) {
            if (!PlaceholderSyntax.isValidToken(token)) {
                if (warnedInvalid.add(token)) {
                    logger.warn("[UXAddon][Action:placeholder] Cause: InvalidPlaceholderToken token=" + token);
                }
                return input;
            }
        }

        String withBuiltin = normalized.replace("${player_name}", safe(player.getName()));

        if (!PlaceholderApiHook.isAvailable()) {
            return withBuiltin;
        }

        String papiInput = PlaceholderSyntax.toPlaceholderApi(withBuiltin);
        String expanded = PlaceholderApiHook.apply(player, papiInput);

        for (String token : PlaceholderSyntax.extractPercentTokens(expanded)) {
            if (warnedUnresolved.add(token)) {
                logger.warn("[UXAddon][Action:placeholder] Cause: PlaceholderNotRecognized token=%" + token + "%");
            }
        }

        return PlaceholderSyntax.toDollarFromPlaceholderApi(expanded);
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }
}
