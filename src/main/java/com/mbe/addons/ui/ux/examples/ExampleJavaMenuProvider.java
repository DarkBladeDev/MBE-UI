package com.mbe.addons.ui.ux.examples;

import com.mbe.addons.ui.ux.engine.MenuProvider;
import com.mbe.addons.ui.ux.engine.model.ActionCall;
import com.mbe.addons.ui.ux.engine.model.MenuDefinition;
import com.mbe.addons.ui.ux.engine.model.SlotDefinition;
import com.mbe.addons.ui.ux.engine.runtime.MenuContext;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class ExampleJavaMenuProvider implements MenuProvider {
    @Override
    public MenuDefinition create(MenuContext ctx) {
        return new MenuDefinition(
                "example:java",
                "&dJava Menu (%player_name%)",
                3,
                Map.of(
                        11,
                        new SlotDefinition(
                                "minecraft:paper",
                                "&aRun Command",
                                List.of("&7Runs as console: &f/say"),
                                List.of(new ActionCall("run_command", Map.of("as", "console", "command", "say Hello %player_name%"))),
                                Map.of()
                        ),
                        15,
                        new SlotDefinition(
                                "minecraft:barrier",
                                "&cClose",
                                List.of(),
                                List.of(new ActionCall("close", Map.of())),
                                Map.of()
                        )
                ),
                Optional.empty()
        );
    }
}
