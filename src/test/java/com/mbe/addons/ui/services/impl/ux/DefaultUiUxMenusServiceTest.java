package com.mbe.addons.ui.services.impl.ux;

import com.mbe.addons.ui.ux.engine.MenuContextFactory;
import com.mbe.addons.ui.ux.engine.MenuProvider;
import com.mbe.addons.ui.ux.engine.model.MenuDefinition;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

final class DefaultUiUxMenusServiceTest {

    @Test
    void loadMenus_delegatesToEngine() {
        AtomicBoolean called = new AtomicBoolean(false);
        DefaultUiUxMenusService service = new DefaultUiUxMenusService(
                () -> called.set(true),
                () -> Path.of("menus"),
                id -> Optional.empty(),
                (id, provider) -> {
                },
                (id, factory) -> {
                }
        );

        service.loadMenus();

        assertEquals(true, called.get());
    }

    @Test
    void menusDir_returnsEngineValue() {
        Path dir = Path.of("menus");

        DefaultUiUxMenusService service = new DefaultUiUxMenusService(
                () -> {
                },
                () -> dir,
                id -> Optional.empty(),
                (id, provider) -> {
                },
                (id, factory) -> {
                }
        );

        assertSame(dir, service.menusDir());
    }

    @Test
    void getMenuDefinition_returnsEngineValue() {
        Optional<MenuDefinition> expected = Optional.empty();

        DefaultUiUxMenusService service = new DefaultUiUxMenusService(
                () -> {
                },
                () -> Path.of("menus"),
                id -> expected,
                (id, provider) -> {
                },
                (id, factory) -> {
                }
        );

        assertSame(expected, service.getMenuDefinition("example:main"));
    }

    @Test
    void registerMenuProvider_delegatesToEngine() {
        AtomicReference<String> seenId = new AtomicReference<>();
        AtomicReference<MenuProvider> seenProvider = new AtomicReference<>();

        DefaultUiUxMenusService service = new DefaultUiUxMenusService(
                () -> {
                },
                () -> Path.of("menus"),
                id -> Optional.empty(),
                (id, provider) -> {
                    seenId.set(id);
                    seenProvider.set(provider);
                },
                (id, factory) -> {
                }
        );

        MenuProvider provider = ctx -> null;

        service.registerMenuProvider("example:main", provider);

        assertEquals("example:main", seenId.get());
        assertSame(provider, seenProvider.get());
    }

    @Test
    void open_delegatesToEngine() {
        AtomicReference<String> seenId = new AtomicReference<>();
        AtomicReference<MenuContextFactory> seenFactory = new AtomicReference<>();

        DefaultUiUxMenusService service = new DefaultUiUxMenusService(
                () -> {
                },
                () -> Path.of("menus"),
                id -> Optional.empty(),
                (id, provider) -> {
                },
                (id, factory) -> {
                    seenId.set(id);
                    seenFactory.set(factory);
                }
        );

        MenuContextFactory contextFactory = new MenuContextFactory() {
            @Override
            public org.bukkit.entity.Player player() {
                return null;
            }

            @Override
            public java.util.Map<String, Object> variables() {
                return null;
            }

            @Override
            public java.util.Optional<com.darkbladedev.engine.model.MultiblockInstance> multiblock() {
                return null;
            }
        };

        service.open("example:main", contextFactory);

        assertEquals("example:main", seenId.get());
        assertSame(contextFactory, seenFactory.get());
    }
}
