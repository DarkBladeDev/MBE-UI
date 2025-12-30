package com.mbe.addons.ui.ux.placeholders;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Conversión y normalización de placeholders.
 *
 * <p>Reglas del sistema:</p>
 * <ul>
 *   <li>Sintaxis canónica interna: <code>${token}</code></li>
 *   <li>Sintaxis legacy/externa: <code>%token%</code> (PlaceholderAPI)</li>
 * </ul>
 *
 * <p>El runtime acepta ambas formas para mantener compatibilidad, pero:</p>
 * <ul>
 *   <li>Normaliza <code>%token%</code> → <code>${token}</code> al ingresar</li>
 *   <li>Para resolver con PlaceholderAPI, convierte <code>${token}</code> → <code>%token%</code>,
 *       ejecuta el parseo y luego vuelve a <code>${token}</code> cualquier placeholder no resuelto</li>
 * </ul>
 */
public final class PlaceholderSyntax {
    private static final Pattern PERCENT = Pattern.compile("%([^%\\s]+)%");
    private static final Pattern DOLLAR = Pattern.compile("\\$\\{([^}\\s]+)}");

    private PlaceholderSyntax() {
    }

    public static String normalizeToDollar(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        return replaceAll(PERCENT, input, m -> "${" + m.group(1) + "}");
    }

    public static String toPlaceholderApi(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        return replaceAll(DOLLAR, input, m -> "%" + m.group(1) + "%");
    }

    public static String toDollarFromPlaceholderApi(String input) {
        return normalizeToDollar(input);
    }

    public static List<String> extractDollarTokens(String input) {
        if (input == null || input.isEmpty()) {
            return List.of();
        }
        Matcher m = DOLLAR.matcher(input);
        List<String> out = new ArrayList<>();
        while (m.find()) {
            out.add(m.group(1));
        }
        return out;
    }

    public static List<String> extractPercentTokens(String input) {
        if (input == null || input.isEmpty()) {
            return List.of();
        }
        Matcher m = PERCENT.matcher(input);
        List<String> out = new ArrayList<>();
        while (m.find()) {
            out.add(m.group(1));
        }
        return out;
    }

    public static boolean isValidToken(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        for (int i = 0; i < token.length(); i++) {
            char c = token.charAt(i);
            if (Character.isWhitespace(c) || c == '%' || c == '{' || c == '}') {
                return false;
            }
        }
        return true;
    }

    private static String replaceAll(Pattern pattern, String input, java.util.function.Function<Matcher, String> replacement) {
        Objects.requireNonNull(pattern, "pattern");
        Objects.requireNonNull(replacement, "replacement");

        Matcher m = pattern.matcher(input);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String rep = replacement.apply(m);
            m.appendReplacement(sb, Matcher.quoteReplacement(rep));
        }
        m.appendTail(sb);
        return sb.toString();
    }
}

