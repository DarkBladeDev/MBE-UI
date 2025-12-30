package com.mbe.addons.ui.ux.engine.parse;

import com.mbe.addons.ui.ux.engine.model.ActionCall;
import com.mbe.addons.ui.ux.engine.model.MenuDefinition;
import com.mbe.addons.ui.ux.engine.model.PaginationDefinition;
import com.mbe.addons.ui.ux.engine.model.SlotDefinition;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
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
import java.util.logging.Level;
import java.util.logging.Logger;

public final class MenuParser {
    private final String addonId;
    private final Logger logger;
    private final MenuSchemaValidator validator;

    public MenuParser(String addonId, Logger logger) {
        this.addonId = Objects.requireNonNull(addonId, "addonId");
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

        resolveExternalMenusDir().ifPresent(dir -> {
            if (!Files.isDirectory(dir)) {
                return;
            }
            try (var stream = Files.list(dir)) {
                stream
                        .filter(p -> {
                            String n = p.getFileName().toString().toLowerCase();
                            return n.endsWith(".yml") || n.endsWith(".yaml");
                        })
                        .forEach(p -> loadFromFile(p.toFile()).ifPresent(out::add));
            } catch (Exception e) {
                logger.log(Level.WARNING, "[UXAddon] Failed listing menus directory: " + dir, e);
            }
        });

        return out;
    }

    private Optional<MenuDefinition> loadFromResource(String resourcePath) {
        try (var in = MenuParser.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (in == null) {
                return Optional.empty();
            }

            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(new InputStreamReader(in, StandardCharsets.UTF_8));
            return parse(yaml, resourcePath);
        } catch (Exception e) {
            logger.log(Level.WARNING, "[UXAddon] Failed loading menu resource: " + resourcePath, e);
            return Optional.empty();
        }
    }

    private Optional<MenuDefinition> loadFromFile(File file) {
        try {
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
            return parse(yaml, file.getName());
        } catch (Exception e) {
            logger.log(Level.WARNING, "[UXAddon] Failed loading menu file: " + file.getAbsolutePath(), e);
            return Optional.empty();
        }
    }

    private Optional<MenuDefinition> parse(YamlConfiguration yaml, String sourceName) {
        Map<String, Object> raw = yaml.getValues(true);
        List<String> errors = validator.validate(yaml);
        if (!errors.isEmpty()) {
            String id = yaml.getString("id", "<missing>");
            logger.warning("[UXAddon] YAML invalid, menu disabled. source=" + sourceName + " id=" + id + " errors=" + errors);
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

        return Optional.of(new MenuDefinition(id, title, rows, slots, pagination));
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

    private Optional<Path> resolveExternalMenusDir() {
        try {
            var location = MenuParser.class.getProtectionDomain().getCodeSource().getLocation();
            if (location == null) {
                return Optional.empty();
            }
            Path self = Path.of(location.toURI());
            Path baseDir = Files.isDirectory(self) ? self : self.getParent();
            if (baseDir == null) {
                return Optional.empty();
            }
            return Optional.of(baseDir.resolve("menus"));
        } catch (URISyntaxException e) {
            return Optional.empty();
        }
    }
}

