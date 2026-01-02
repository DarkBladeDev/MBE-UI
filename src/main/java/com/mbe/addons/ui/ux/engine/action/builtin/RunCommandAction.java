package com.mbe.addons.ui.ux.engine.action.builtin;

import com.mbe.addons.ui.ux.engine.action.MenuAction;
import com.mbe.addons.ui.ux.engine.runtime.MenuContext;
import com.mbe.addons.ui.ux.placeholders.PlaceholderProcessor;

import com.darkbladedev.engine.api.logging.EngineLogger;

import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class RunCommandAction implements MenuAction {
    private static final Pattern VAR = Pattern.compile("<variable:([^>]+)>");

    private final EngineLogger logger;

    public RunCommandAction(EngineLogger logger) {
        this.logger = Objects.requireNonNull(logger, "logger");
    }

    @Override
    public void execute(MenuContext ctx, Map<String, Object> args) {
        Objects.requireNonNull(ctx, "ctx");
        Objects.requireNonNull(args, "args");

        Object cmdObj = args.get("command");
        if (!(cmdObj instanceof String command) || command.isBlank()) {
            return;
        }

        String as = String.valueOf(args.getOrDefault("as", "player"));
        String resolved = replaceVars(command, ctx.variables());
        resolved = PlaceholderProcessor.process(logger, ctx.player(), resolved);

        if ("console".equalsIgnoreCase(as)) {
            ctx.player().getServer().dispatchCommand(ctx.player().getServer().getConsoleSender(), resolved);
        } else {
            ctx.player().performCommand(resolved);
        }
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
