package com.mbe.addons.ui.ux.engine.runtime;

import com.darkbladedev.engine.api.addon.AddonContext;
import com.darkbladedev.engine.api.logging.EngineLogger;
import com.darkbladedev.engine.api.logging.LogLevel;
import com.darkbladedev.engine.model.MultiblockInstance;
import com.mbe.ui.api.menu.MenuId;
import com.mbe.ui.api.menu.MenuView;
import com.mbe.ui.api.menu.PlayerContext;
import com.mbe.ui.api.menu.UIMenu;
import com.mbe.addons.ui.ui.UI;
import com.mbe.addons.ui.runtime.SessionManager;
import com.mbe.addons.ui.ux.engine.MenuContextFactory;
import com.mbe.addons.ui.ux.engine.MenuEngine;
import com.mbe.addons.ui.ux.engine.MenuProvider;
import com.mbe.addons.ui.ux.engine.model.MenuDefinition;
import com.mbe.addons.ui.ux.engine.model.PaginationDefinition;
import com.mbe.addons.ui.ux.engine.view.MenuRenderer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class MenuRuntime {
    private static final String KEY_MENU_ID = "ux.menuId";
    private static final String KEY_VARS = "ux.vars";
    private static final String KEY_MB = "ux.mb";
    private static final String KEY_PAGE = "ux.page";

    private final AddonContext context;
    private final MenuEngine engine;
    private final MenuRenderer renderer;
    private final EngineLogger logger;
    private final VariableResolver variableResolver;

    private final ThreadLocal<RuntimeFrame> frame = new ThreadLocal<>();

    public MenuRuntime(AddonContext context, MenuEngine engine, MenuRenderer renderer, EngineLogger logger) {
        this.context = Objects.requireNonNull(context, "context");
        this.engine = Objects.requireNonNull(engine, "engine");
        this.renderer = Objects.requireNonNull(renderer, "renderer");
        this.logger = Objects.requireNonNull(logger, "logger");
        this.variableResolver = new VariableResolver(this.logger);
    }

    public void open(String menuId, MenuContextFactory factory) {
        Objects.requireNonNull(menuId, "menuId");
        Objects.requireNonNull(factory, "factory");

        context.runTask(() -> {
            Map<String, Object> vars = factory.variables() == null ? Map.of() : Map.copyOf(factory.variables());
            Optional<MultiblockInstance> mb = factory.multiblock() == null ? Optional.empty() : factory.multiblock();

            Map<String, Object> tmpSession = new HashMap<>();
            tmpSession.put(KEY_MENU_ID, menuId);
            tmpSession.put(KEY_VARS, vars);
            tmpSession.put(KEY_MB, mb.orElse(null));
            tmpSession.put(KEY_PAGE, 0);

            MenuContext openCtx = new RuntimeBackedMenuContext(factory.player(), menuId, 0, vars, mb, tmpSession);
            MenuDefinition def = resolveDefinition(menuId, openCtx).orElse(null);
            int size = def != null ? def.size() : 27;
            UIMenu menu = new DeclarativeMenu(menuId, size, this);

            if (UI.controller() instanceof SessionManager sm) {
                sm.open(menu, factory.player(), tmpSession);
            } else {
                UI.controller().open(menu, factory.player());
            }

            UI.controller().refresh(factory.player());
        });
    }

    public MenuView render(PlayerContext uiCtx) {
        Objects.requireNonNull(uiCtx, "uiCtx");

        Map<String, Object> session = uiCtx.sessionData();
        String menuId = String.valueOf(session.getOrDefault(KEY_MENU_ID, ""));
        if (menuId.isBlank()) {
            return () -> Map.of();
        }

        int page = readInt(session.get(KEY_PAGE), 0);
        Map<String, Object> baseVars = readVars(session.get(KEY_VARS));
        Optional<MultiblockInstance> mb = readMultiblock(session.get(KEY_MB));

        Map<String, Object> vars = withRuntimeVars(baseVars, session, page);
        RuntimeBackedMenuContext ctx = new RuntimeBackedMenuContext(uiCtx.player(), menuId, page, vars, mb, session);
        MenuDefinition def = resolveDefinition(menuId, ctx).orElse(null);
        if (def == null) {
            logger.warn("[UXAddon][Menu:" + menuId + "][Action:render] Cause: MenuNotFoundException");
            return () -> Map.of();
        }

        if (def.rows() < 1 || def.rows() > 6) {
            logger.warn("[UXAddon][Menu:" + menuId + "][Action:render] Cause: InvalidRows");
            return () -> Map.of();
        }

        List<Map<String, Object>> paged = List.of();
        if (def.pagination().isPresent()) {
            PaginationDefinition p = def.pagination().get();
            List<Map<String, Object>> all = resolvePaginationEntries(ctx, p);
            int pageSize = Math.max(1, p.pageSize());
            int totalPages = (all.size() + pageSize - 1) / pageSize;
            if (totalPages <= 0) {
                totalPages = 1;
            }
            int clamped = Math.max(0, Math.min(page, totalPages - 1));
            if (clamped != page) {
                session.put(KEY_PAGE, clamped);
                vars = withRuntimeVars(baseVars, session, clamped);
                ctx = new RuntimeBackedMenuContext(uiCtx.player(), menuId, clamped, vars, mb, session);
            }

            int from = clamped * pageSize;
            int to = Math.min(from + pageSize, all.size());
            if (from < to) {
                paged = all.subList(from, to);
            }

            session.put("ux.total_pages", totalPages);
            session.put("ux.total_items", all.size());
        }

        RenderVariableCache cache = new RenderVariableCache();
        return renderer.render(this, def, paged, ctx, cache);
    }

    String resolveText(String input, MenuContext ctx, RenderVariableCache cache) {
        return variableResolver.resolve(input, ctx, cache);
    }

    public MenuEngine engine() {
        return engine;
    }

    public MenuRuntimeOperations operations() {
        return new MenuRuntimeOperations() {
            @Override
            public void enter(MenuContext ctx) {
                if (ctx instanceof RuntimeBackedMenuContext internal) {
                    frame.set(new RuntimeFrame(internal));
                }
            }

            @Override
            public void exit() {
                frame.remove();
            }

            @Override
            public void openMenu(String menuId) {
                RuntimeFrame f = frame.get();
                if (f == null) {
                    return;
                }
                MenuContextFactory factory = new MenuContextFactory() {
                    @Override
                    public org.bukkit.entity.Player player() {
                        return f.ctx.player();
                    }

                    @Override
                    public Map<String, Object> variables() {
                        return f.ctx.variables();
                    }

                    @Override
                    public Optional<MultiblockInstance> multiblock() {
                        return f.ctx.multiblock();
                    }
                };

                open(menuId, factory);
            }

            @Override
            public void close() {
                RuntimeFrame f = frame.get();
                if (f == null) {
                    return;
                }
                UI.controller().close(f.ctx.player());
            }

            @Override
            public void refresh() {
                RuntimeFrame f = frame.get();
                if (f == null) {
                    return;
                }
                UI.controller().refresh(f.ctx.player());
            }

            @Override
            public void nextPage() {
                RuntimeFrame f = frame.get();
                if (f == null) {
                    return;
                }

                int current = readInt(f.ctx.session().get(KEY_PAGE), 0);
                int total = readInt(f.ctx.session().get("ux.total_pages"), 1);
                int next = Math.min(current + 1, Math.max(0, total - 1));
                f.ctx.session().put(KEY_PAGE, next);
                refresh();
            }

            @Override
            public void previousPage() {
                RuntimeFrame f = frame.get();
                if (f == null) {
                    return;
                }
                int current = readInt(f.ctx.session().get(KEY_PAGE), 0);
                int next = Math.max(0, current - 1);
                f.ctx.session().put(KEY_PAGE, next);
                refresh();
            }
        };
    }

    Optional<MenuDefinition> resolveDefinition(String menuId, MenuContext ctx) {
        Optional<MenuProvider> provider = engine.getMenuProvider(menuId);
        if (provider.isPresent()) {
            try {
                MenuDefinition def = provider.get().create(ctx);
                return Optional.ofNullable(def);
            } catch (Throwable t) {
                logger.log(LogLevel.WARN, "[UXAddon][Menu:" + menuId + "][Action:provider] Cause: " + t.getClass().getSimpleName(), t);
            }
        }
        return engine.getMenuDefinition(menuId);
    }

    private List<Map<String, Object>> resolvePaginationEntries(MenuContext ctx, PaginationDefinition p) {
        Object v = resolvePath(ctx.variables(), p.source());
        if (v == null) {
            return List.of();
        }

        List<Map<String, Object>> out = new ArrayList<>();
        if (v instanceof Collection<?> col) {
            int index = 0;
            for (Object o : col) {
                if (o instanceof Map<?, ?> m) {
                    Map<String, Object> entry = new HashMap<>();
                    for (var e : m.entrySet()) {
                        if (e.getKey() instanceof String k) {
                            entry.put(k, e.getValue());
                        }
                    }
                    entry.putIfAbsent("entry_index", index);
                    out.add(entry);
                } else {
                    out.add(Map.of("entry_name", String.valueOf(o), "entry_index", index));
                }
                index++;
            }
        }
        return out;
    }

    private Map<String, Object> withRuntimeVars(Map<String, Object> baseVars, Map<String, Object> session, int page) {
        Map<String, Object> vars = new HashMap<>(baseVars);
        vars.put("page", page + 1);
        vars.put("total_pages", readInt(session.get("ux.total_pages"), 1));
        vars.put("total_items", readInt(session.get("ux.total_items"), 0));
        return Map.copyOf(vars);
    }

    private Object resolvePath(Map<String, Object> vars, String path) {
        if (vars == null || path == null || path.isBlank()) {
            return null;
        }
        String[] parts = path.split("\\.");
        Object current = vars;
        for (String part : parts) {
            if (!(current instanceof Map<?, ?> m)) {
                return null;
            }
            current = m.get(part);
        }
        return current;
    }

    private int readInt(Object value, int fallback) {
        if (value instanceof Number n) {
            return n.intValue();
        }
        if (value instanceof String s) {
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException ignored) {
                return fallback;
            }
        }
        return fallback;
    }

    private Map<String, Object> readVars(Object value) {
        if (value instanceof Map<?, ?> m) {
            Map<String, Object> out = new HashMap<>();
            for (var e : m.entrySet()) {
                if (e.getKey() instanceof String k) {
                    out.put(k, e.getValue());
                }
            }
            return Map.copyOf(out);
        }
        return Map.of();
    }

    private Optional<MultiblockInstance> readMultiblock(Object value) {
        if (value instanceof MultiblockInstance mb) {
            return Optional.of(mb);
        }
        return Optional.empty();
    }

    private record RuntimeFrame(RuntimeBackedMenuContext ctx) {
    }

    private static final class DeclarativeMenu implements UIMenu {
        private final String menuId;
        private final int size;
        private final MenuRuntime runtime;

        private DeclarativeMenu(String menuId, int size, MenuRuntime runtime) {
            this.menuId = menuId;
            this.size = size;
            this.runtime = runtime;
        }

        @Override
        public MenuId id() {
            String[] parts = menuId.split(":", 2);
            if (parts.length == 2) {
                return new MenuId(parts[0], parts[1]);
            }
            return new MenuId("menu", menuId);
        }

        @Override
        public String title(PlayerContext ctx) {
            Map<String, Object> session = ctx.sessionData();
            Object v = session.getOrDefault("ux.title", null);
            if (v instanceof String s && !s.isBlank()) {
                return s;
            }
            MenuContext menuCtx = runtime.buildContext(ctx);
            MenuDefinition def = runtime.resolveDefinition(menuId, menuCtx).orElse(null);
            if (def == null) {
                return menuId;
            }
            RenderVariableCache cache = new RenderVariableCache();
            return runtime.resolveText(def.title(), menuCtx, cache);
        }

        @Override
        public int size() {
            return size;
        }

        @Override
        public MenuView render(PlayerContext ctx) {
            return runtime.render(ctx);
        }
    }

    MenuContext buildContext(PlayerContext uiCtx) {
        Map<String, Object> session = uiCtx.sessionData();
        String menuId = String.valueOf(session.getOrDefault(KEY_MENU_ID, ""));
        int page = readInt(session.get(KEY_PAGE), 0);
        Map<String, Object> baseVars = readVars(session.get(KEY_VARS));
        Optional<MultiblockInstance> mb = readMultiblock(session.get(KEY_MB));
        Map<String, Object> vars = withRuntimeVars(baseVars, session, page);
        return new RuntimeBackedMenuContext(uiCtx.player(), menuId, page, vars, mb, session);
    }
}
