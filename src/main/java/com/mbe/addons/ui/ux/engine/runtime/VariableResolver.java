package com.mbe.addons.ui.ux.engine.runtime;

import com.mbe.addons.ui.ux.placeholders.PlaceholderProcessor;

import com.darkbladedev.engine.api.logging.EngineLogger;

import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class VariableResolver {
    private static final Pattern VAR = Pattern.compile("<variable:([^>]+)>");

    private final EngineLogger logger;

    VariableResolver(EngineLogger logger) {
        this.logger = Objects.requireNonNull(logger, "logger");
    }

    String resolve(String input, MenuContext ctx, RenderVariableCache cache) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        Objects.requireNonNull(ctx, "ctx");
        Objects.requireNonNull(cache, "cache");

        String resolved = cache.get("raw:" + input
                + "|p:" + ctx.player().getUniqueId()
                + "|m:" + ctx.menuId()
                + "|pg:" + ctx.page()
                + "|v:" + System.identityHashCode(ctx.variables()), () -> {
            String withVars = replaceVars(input, ctx.variables());
            return PlaceholderProcessor.process(logger, ctx.player(), withVars);
        });

        return resolved;
    }

    private String replaceVars(String input, Map<String, Object> vars) {
        Matcher m = VAR.matcher(input);
        StringBuffer sb = new StringBuffer();

        while (m.find()) {
            String key = m.group(1);
            Object v = vars.get(key);
            String rep = v == null ? "" : String.valueOf(v);
            m.appendReplacement(sb, Matcher.quoteReplacement(rep));
        }
        m.appendTail(sb);
        return sb.toString();
    }
}
