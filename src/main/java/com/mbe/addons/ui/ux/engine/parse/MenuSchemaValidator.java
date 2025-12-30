package com.mbe.addons.ui.ux.engine.parse;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.ArrayList;
import java.util.List;

final class MenuSchemaValidator {
    List<String> validate(YamlConfiguration yaml) {
        List<String> errors = new ArrayList<>();

        String id = yaml.getString("id", null);
        if (id == null || id.isBlank() || !id.contains(":")) {
            errors.add("id");
        }

        String title = yaml.getString("title", null);
        if (title == null || title.isBlank()) {
            errors.add("title");
        }

        int rows = yaml.getInt("rows", -1);
        if (rows < 1 || rows > 6) {
            errors.add("rows");
        }

        ConfigurationSection slots = yaml.getConfigurationSection("slots");
        if (slots == null) {
            errors.add("slots");
        } else {
            int size = rows > 0 ? rows * 9 : 0;
            for (String key : slots.getKeys(false)) {
                int slot;
                try {
                    slot = Integer.parseInt(key);
                } catch (NumberFormatException e) {
                    errors.add("slots." + key);
                    continue;
                }

                if (slot < 0 || slot >= size) {
                    errors.add("slots." + key);
                    continue;
                }

                ConfigurationSection slotSection = slots.getConfigurationSection(key);
                if (slotSection == null) {
                    errors.add("slots." + key);
                    continue;
                }

                String item = slotSection.getString("item", null);
                if (item == null || item.isBlank()) {
                    errors.add("slots." + key + ".item");
                }
            }
        }

        ConfigurationSection pagination = yaml.getConfigurationSection("pagination");
        if (pagination != null) {
            String source = pagination.getString("source", null);
            if (source == null || source.isBlank()) {
                errors.add("pagination.source");
            }
            int pageSize = pagination.getInt("page_size", -1);
            if (pageSize <= 0) {
                errors.add("pagination.page_size");
            }
        }

        return errors;
    }
}

