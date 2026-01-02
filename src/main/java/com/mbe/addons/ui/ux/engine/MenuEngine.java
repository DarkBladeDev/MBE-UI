package com.mbe.addons.ui.ux.engine;

import com.darkbladedev.engine.api.addon.AddonContext;
import com.darkbladedev.engine.api.logging.EngineLogger;
import com.darkbladedev.engine.api.logging.LogLevel;
import com.mbe.addons.ui.ui.UI;
import com.mbe.addons.ui.ux.engine.action.MenuActionRegistry;
import com.mbe.addons.ui.ux.engine.action.builtin.CloseAction;
import com.mbe.addons.ui.ux.engine.action.builtin.NextPageAction;
import com.mbe.addons.ui.ux.engine.action.builtin.OpenMenuAction;
import com.mbe.addons.ui.ux.engine.action.builtin.PreviousPageAction;
import com.mbe.addons.ui.ux.engine.action.builtin.RunCommandAction;
import com.mbe.addons.ui.ux.engine.model.ActionCall;
import com.mbe.addons.ui.ux.engine.model.MenuDefinition;
import com.mbe.addons.ui.ux.engine.model.PaginationDefinition;
import com.mbe.addons.ui.ux.engine.model.SlotDefinition;
import com.mbe.addons.ui.ux.engine.parse.MenuParser;
import com.mbe.addons.ui.ux.engine.runtime.MenuRuntime;
import com.mbe.addons.ui.ux.engine.runtime.MenuRuntimeOperations;
import com.mbe.addons.ui.ux.engine.view.MenuRenderer;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class MenuEngine {
    private final AddonContext context;
    private final EngineLogger logger;
    private final Path menusDir;

    private final MenuParser parser;
    private final MenuRenderer renderer;
    private final MenuRuntime runtime;
    private final MenuActionRegistry actionRegistry;

    private final Map<String, MenuDefinition> menus = new ConcurrentHashMap<>();
    private final Map<String, MenuProvider> providers = new ConcurrentHashMap<>();

    public MenuEngine(AddonContext context) {
        this.context = Objects.requireNonNull(context, "context");
        this.logger = context.getLogger();

        this.menusDir = resolveMenusDir(context);

        this.parser = new MenuParser(menusDir, logger);
        this.renderer = new MenuRenderer(logger);
        this.runtime = new MenuRuntime(context, this, renderer, logger);

        MenuRuntimeOperations ops = runtime.operations();
        this.actionRegistry = new MenuActionRegistry(logger, ops);
        registerBaseActions();
    }

    public void loadMenus() {
        Collection<MenuDefinition> parsed = parser.loadAll();
        Map<String, MenuDefinition> next = new ConcurrentHashMap<>();

        for (MenuDefinition def : parsed) {
            String id = def.id();
            if (id == null || id.isBlank()) {
                continue;
            }
            next.put(id, def);
        }

        menus.clear();
        menus.putAll(next);
    }

    public Optional<MenuDefinition> upsertMenuDefinition(MenuDefinition def, boolean persist) {
        Objects.requireNonNull(def, "def");
        if (!persist) {
            menus.put(def.id(), def);
            return Optional.of(def);
        }
        return saveMenu(def);
    }

    public Optional<MenuDefinition> saveMenuYaml(String fileName, String yamlText) {
        Objects.requireNonNull(fileName, "fileName");
        Objects.requireNonNull(yamlText, "yamlText");

        Optional<MenuDefinition> def = writeMenuYamlToDisk(fileName, yamlText);
        def.ifPresent(d -> menus.put(d.id(), d));
        return def;
    }

    public void saveMenuYamlAsync(String fileName, String yamlText) {
        Objects.requireNonNull(fileName, "fileName");
        Objects.requireNonNull(yamlText, "yamlText");

        context.runTaskAsync(() -> {
            Optional<MenuDefinition> def = writeMenuYamlToDisk(fileName, yamlText);
            def.ifPresent(value -> context.runTask(() -> menus.put(value.id(), value)));
        });
    }

    public Optional<MenuDefinition> saveMenu(MenuDefinition def) {
        Objects.requireNonNull(def, "def");
        String fileName = safeFileNameForId(def.id());
        String yamlText = toYaml(def);
        return saveMenuYaml(fileName, yamlText);
    }

    public void registerMenuProvider(String menuId, MenuProvider provider) {
        Objects.requireNonNull(menuId, "menuId");
        Objects.requireNonNull(provider, "provider");
        providers.put(menuId, provider);
    }

    public Optional<MenuDefinition> getMenuDefinition(String menuId) {
        return Optional.ofNullable(menus.get(menuId));
    }

    public Optional<MenuProvider> getMenuProvider(String menuId) {
        return Optional.ofNullable(providers.get(menuId));
    }

    public MenuActionRegistry actions() {
        return actionRegistry;
    }

    public MenuRuntime runtime() {
        return runtime;
    }

    public AddonContext context() {
        return context;
    }

    public Path menusDir() {
        return menusDir;
    }

    public void open(String menuId, MenuContextFactory contextFactory) {
        try {
            runtime.open(menuId, contextFactory);
        } catch (Throwable t) {
            logger.error("[UXAddon][Menu:" + menuId + "][Action:open_menu] Cause: " + t.getClass().getSimpleName(), t);
        }
    }

    public void close(org.bukkit.entity.Player player) {
        UI.controller().close(player);
    }

    private void registerBaseActions() {
        MenuRuntimeOperations ops = runtime.operations();
        actionRegistry.register("open_menu", new OpenMenuAction(ops));
        actionRegistry.register("close", new CloseAction(ops));
        actionRegistry.register("next_page", new NextPageAction(ops));
        actionRegistry.register("previous_page", new PreviousPageAction(ops));
        actionRegistry.register("run_command", new RunCommandAction(logger));
    }

    public static Path resolveMenusDir(AddonContext context) {
        Objects.requireNonNull(context, "context");
        return context.getDataFolder().resolve("menus");
    }

    private boolean ensureWritableMenusDir() {
        try {
            Files.createDirectories(menusDir);
        } catch (Exception e) {
            logger.log(LogLevel.WARN, "[UXAddon][FS] Cannot create menus directory: " + menusDir, e);
            return false;
        }
        if (!Files.isDirectory(menusDir) || !Files.isWritable(menusDir)) {
            logger.warn("[UXAddon][FS] Menus directory not writable: " + menusDir);
            return false;
        }
        return true;
    }

    private Optional<MenuDefinition> writeMenuYamlToDisk(String fileName, String yamlText) {
        if (yamlText.length() > 100_000) {
            logger.warn("[UXAddon][FS] Menu YAML too large, rejected. file=" + fileName);
            return Optional.empty();
        }

        Path target;
        try {
            target = resolveMenuFile(fileName);
        } catch (IllegalArgumentException e) {
            logger.warn("[UXAddon][FS] Invalid menu file name, rejected. file=" + fileName);
            return Optional.empty();
        }

        if (!ensureWritableMenusDir()) {
            return Optional.empty();
        }

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(new StringReader(yamlText));
        Optional<MenuDefinition> def = parser.parseYaml(yaml, fileName);
        if (def.isEmpty()) {
            return Optional.empty();
        }

        try {
            Files.writeString(target, yamlText, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Exception e) {
            logger.log(LogLevel.WARN, "[UXAddon][FS] Failed writing menu YAML. file=" + target, e);
            return Optional.empty();
        }

        return def;
    }

    private Path resolveMenuFile(String fileName) {
        String f = fileName.trim();
        if (f.isEmpty()) {
            throw new IllegalArgumentException("blank");
        }
        f = f.replace('\\', '/');
        if (f.contains("/") || f.contains("..")) {
            throw new IllegalArgumentException("pathTraversal");
        }
        if (!(f.toLowerCase().endsWith(".yml") || f.toLowerCase().endsWith(".yaml"))) {
            f = f + ".yml";
        }
        Path resolved = menusDir.resolve(f).normalize();
        if (!resolved.startsWith(menusDir.normalize())) {
            throw new IllegalArgumentException("escape");
        }
        return resolved;
    }

    private String safeFileNameForId(String menuId) {
        String base = menuId == null ? "menu" : menuId.trim();
        if (base.isEmpty()) {
            base = "menu";
        }
        base = base.replace(':', '-');
        base = base.replaceAll("[^a-zA-Z0-9_.-]", "-");
        return base + ".yml";
    }

    private String toYaml(MenuDefinition def) {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("id", def.id());
        yaml.set("title", def.title());
        yaml.set("rows", def.rows());

        Map<String, Object> slots = new HashMap<>();
        for (var e : def.slots().entrySet()) {
            Map<String, Object> slot = new HashMap<>();
            SlotDefinition s = e.getValue();
            slot.put("item", s.item());
            if (s.name() != null) {
                slot.put("name", s.name());
            }
            if (s.lore() != null && !s.lore().isEmpty()) {
                slot.put("lore", new ArrayList<>(s.lore()));
            }

            if (s.actions() != null && !s.actions().isEmpty()) {
                List<Map<String, Object>> actions = new ArrayList<>();
                for (ActionCall call : s.actions()) {
                    if (call == null) {
                        continue;
                    }
                    Map<String, Object> a = new HashMap<>();
                    a.put("action", call.action());
                    if (call.args() != null && !call.args().isEmpty()) {
                        a.put("args", new HashMap<>(call.args()));
                    }
                    actions.add(a);
                }
                slot.put("actions", actions);
            }

            if (s.extra() != null && !s.extra().isEmpty()) {
                for (var ex : s.extra().entrySet()) {
                    slot.put(ex.getKey(), ex.getValue());
                }
            }

            slots.put(String.valueOf(e.getKey()), slot);
        }
        if (!slots.isEmpty()) {
            yaml.set("slots", slots);
        }

        if (def.pagination().isPresent()) {
            PaginationDefinition p = def.pagination().get();
            Map<String, Object> pagination = new HashMap<>();
            pagination.put("source", p.source());
            pagination.put("page_size", p.pageSize());
            pagination.put("from_slot", p.fromSlot());
            pagination.put("to_slot", p.toSlot());
            if (p.template() != null && p.template().isPresent()) {
                Map<String, Object> t = new HashMap<>();
                SlotDefinition ts = p.template().get();
                t.put("item", ts.item());
                if (ts.name() != null) {
                    t.put("name", ts.name());
                }
                if (ts.lore() != null && !ts.lore().isEmpty()) {
                    t.put("lore", new ArrayList<>(ts.lore()));
                }
                if (ts.actions() != null && !ts.actions().isEmpty()) {
                    List<Map<String, Object>> actions = new ArrayList<>();
                    for (ActionCall call : ts.actions()) {
                        if (call == null) {
                            continue;
                        }
                        Map<String, Object> a = new HashMap<>();
                        a.put("action", call.action());
                        if (call.args() != null && !call.args().isEmpty()) {
                            a.put("args", new HashMap<>(call.args()));
                        }
                        actions.add(a);
                    }
                    t.put("actions", actions);
                }
                if (ts.extra() != null && !ts.extra().isEmpty()) {
                    for (var ex : ts.extra().entrySet()) {
                        t.put(ex.getKey(), ex.getValue());
                    }
                }
                pagination.put("template", t);
            }
            yaml.set("pagination", pagination);
        }

        return yaml.saveToString();
    }
}
