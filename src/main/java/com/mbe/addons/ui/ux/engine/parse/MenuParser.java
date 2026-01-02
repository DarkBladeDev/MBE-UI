package com.mbe.addons.ui.ux.engine.parse;

import com.mbe.addons.ui.ux.engine.model.ActionCall;
import com.mbe.addons.ui.ux.engine.model.MenuDefinition;
import com.mbe.addons.ui.ux.engine.model.PaginationDefinition;
import com.mbe.addons.ui.ux.engine.model.SlotDefinition;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import com.darkbladedev.engine.api.logging.EngineLogger;
import com.darkbladedev.engine.api.logging.LogLevel;

import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

public final class MenuParser {
    private final EngineLogger logger;
    private final MenuSchemaValidator validator;
    private final Path menusDir;

    private static final Pattern SAFE_ACTION_ID = Pattern.compile("[a-z0-9_:\\-]+", Pattern.CASE_INSENSITIVE);

    public MenuParser(Path menusDir, EngineLogger logger) {
        this.menusDir = Objects.requireNonNull(menusDir, "menusDir");
        this.logger = Objects.requireNonNull(logger, "logger");
        this.validator = new MenuSchemaValidator();
    }

    public Collection<MenuDefinition> loadAll() {
        List<MenuDefinition> out = new ArrayList<>();

        for (String resource : List.of(
                "menus/example_main.yml",
                "menus/example_paged.yml"
        )) {
            loadFromResource(resource).ifPresent(out::add);
        }

        loadFromMenusDir(out);

        return out;
    }

    private Optional<MenuDefinition> loadFromResource(String resourcePath) {
        try (var in = MenuParser.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (in == null) {
                return Optional.empty();
            }

            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(new InputStreamReader(in, StandardCharsets.UTF_8));
            return parseYaml(yaml, resourcePath);
        } catch (Exception e) {
            logger.log(LogLevel.WARN, "[UXAddon] Failed loading menu resource: " + resourcePath, e);
            return Optional.empty();
        }
    }

    private Optional<MenuDefinition> loadFromFile(File file) {
        try {
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
            return parseYaml(yaml, file.getName());
        } catch (Exception e) {
            logger.log(LogLevel.WARN, "[UXAddon] Failed loading menu file: " + file.getAbsolutePath(), e);
            return Optional.empty();
        }
    }

    public Optional<MenuDefinition> parseYaml(YamlConfiguration yaml, String sourceName) {
        List<String> errors = validator.validate(yaml);
        if (!errors.isEmpty()) {
            String id = yaml.getString("id", "<missing>");
            logger.warn("[UXAddon] YAML invalid, menu disabled. source=" + sourceName + " id=" + id + " errors=" + errors);
            return Optional.empty();
        }

        String id = Objects.requireNonNull(yaml.getString("id"));
        String title = Objects.requireNonNull(yaml.getString("title"));
        int rows = yaml.getInt("rows");
        int size = rows * 9;

        Map<Integer, SlotDefinition> slots = new HashMap<>();
        ConfigurationSection slotsSection = yaml.getConfigurationSection("slots");
        if (slotsSection != null) {
            for (String key : slotsSection.getKeys(false)) {
                int slot;
                try {
                    slot = Integer.parseInt(key);
                } catch (NumberFormatException e) {
                    continue;
                }
                if (slot < 0 || slot >= size) {
                    continue;
                }

                ConfigurationSection section = slotsSection.getConfigurationSection(key);
                if (section == null) {
                    continue;
                }
                SlotDefinition slotDef = parseSlot(section);
                slots.put(slot, slotDef);
            }
        }

        Optional<PaginationDefinition> pagination = Optional.ofNullable(yaml.getConfigurationSection("pagination"))
                .map(this::parsePagination);

        MenuDefinition def = new MenuDefinition(id, title, rows, slots, pagination);
        List<String> securityErrors = validateSecurity(def);
        if (!securityErrors.isEmpty()) {
            logger.warn("[UXAddon] YAML rejected by security validation. source=" + sourceName + " id=" + id + " errors=" + securityErrors);
            return Optional.empty();
        }

        return Optional.of(def);
    }

    private SlotDefinition parseSlot(ConfigurationSection section) {
        String item = Objects.requireNonNull(section.getString("item"));
        String name = section.getString("name", null);
        List<String> lore = section.getStringList("lore");

        List<ActionCall> actions = new ArrayList<>();
        List<Map<?, ?>> actionMaps = section.getMapList("actions");
        for (Map<?, ?> raw : actionMaps) {
            Object actionObj = raw.get("action");
            if (!(actionObj instanceof String action) || action.isBlank()) {
                continue;
            }

            Map<String, Object> args = new HashMap<>();
            Object argsObj = raw.get("args");
            if (argsObj instanceof Map<?, ?> m) {
                for (var e : m.entrySet()) {
                    if (e.getKey() instanceof String k) {
                        args.put(k, e.getValue());
                    }
                }
            }
            actions.add(new ActionCall(action, args));
        }

        Map<String, Object> extra = new HashMap<>();
        for (String key : section.getKeys(false)) {
            if (key.equals("item") || key.equals("name") || key.equals("lore") || key.equals("actions")) {
                continue;
            }
            extra.put(key, section.get(key));
        }

        return new SlotDefinition(item, name, lore, actions, extra);
    }

    private PaginationDefinition parsePagination(ConfigurationSection section) {
        String source = Objects.requireNonNull(section.getString("source"));
        int pageSize = section.getInt("page_size");
        int fromSlot = section.getInt("from_slot", 0);
        int toSlot = section.getInt("to_slot", fromSlot + pageSize - 1);

        Optional<SlotDefinition> template = Optional.ofNullable(section.getConfigurationSection("template"))
                .map(this::parseSlot);

        return new PaginationDefinition(source, pageSize, fromSlot, toSlot, template);
    }

    private void loadFromMenusDir(List<MenuDefinition> out) {
        if (!Files.isDirectory(menusDir)) {
            return;
        }

        if (!Files.isReadable(menusDir)) {
            logger.warn("[UXAddon][FS] Menus directory not readable: " + menusDir);
            return;
        }

        List<Path> files;
        try {
            files = findMenuFilesRecursively(menusDir);
        } catch (Exception e) {
            logger.log(LogLevel.WARN, "[UXAddon] Failed listing menus directory: " + menusDir, e);
            return;
        }

        for (Path p : files) {
            loadFromFile(p.toFile()).ifPresent(out::add);
        }
    }

    private List<Path> findMenuFilesRecursively(Path root) throws Exception {
        try (var stream = Files.walk(root)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(p -> {
                        String n = p.getFileName().toString().toLowerCase();
                        return n.endsWith(".yml") || n.endsWith(".yaml");
                    })
                    .sorted((a, b) -> a.toString().compareToIgnoreCase(b.toString()))
                    .toList();
        }
    }

    private List<String> validateSecurity(MenuDefinition def) {
        List<String> errors = new ArrayList<>();
        for (SlotDefinition slot : def.slots().values()) {
            for (ActionCall call : slot.actions()) {
                if (call == null) {
                    continue;
                }
                String action = call.action();
                if (action == null || action.isBlank() || !SAFE_ACTION_ID.matcher(action).matches()) {
                    errors.add("InvalidActionId:" + String.valueOf(action));
                    continue;
                }
                if (call.args() != null) {
                    for (var e : call.args().entrySet()) {
                        if (e.getKey() == null || e.getKey().isBlank()) {
                            errors.add("InvalidArgKey:" + action);
                            continue;
                        }
                        Object v = e.getValue();
                        if (!isSafeValue(v)) {
                            errors.add("InvalidArgValue:" + action + ":" + e.getKey());
                        }
                        if ("run_command".equalsIgnoreCase(action) && "command".equalsIgnoreCase(e.getKey()) && v instanceof String s) {
                            if (s.contains("\n") || s.contains("\r")) {
                                errors.add("InvalidCommandNewline");
                            }
                        }
                        if ("open_menu".equalsIgnoreCase(action) && "id".equalsIgnoreCase(e.getKey()) && v instanceof String s) {
                            if (s.isBlank() || s.chars().anyMatch(Character::isWhitespace)) {
                                errors.add("InvalidOpenMenuId");
                            }
                        }
                    }
                }
            }
        }
        return errors;
    }

    private boolean isSafeValue(Object v) {
        if (v == null) {
            return true;
        }
        if (v instanceof String || v instanceof Number || v instanceof Boolean || v instanceof Character) {
            return true;
        }
        if (v instanceof List<?> list) {
            for (Object o : list) {
                if (!isSafeValue(o)) {
                    return false;
                }
            }
            return true;
        }
        if (v instanceof Map<?, ?> map) {
            for (var e : map.entrySet()) {
                if (!(e.getKey() instanceof String) || !isSafeValue(e.getValue())) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
