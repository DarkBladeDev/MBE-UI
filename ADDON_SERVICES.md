# Services entre Addons (registerService / getService)

Este documento explica cómo exponer y consumir servicios entre addons usando el Service Registry del core.

## ¿Qué es un “service” en MultiBlockEngine?

Un service es un objeto que un addon “proveedor” registra para que otros addons puedan obtenerlo a través del `AddonContext`.

- Registrar: `ctx.registerService(Class<T> serviceType, T service)`
- Obtener: `ctx.getService(Class<T> serviceType)`

Nota: estas APIs existen solo en cores que incluyen el Service Registry. En versiones antiguas de MultiBlockEngine pueden no estar presentes.

Contratos:

- `AddonContext`: [AddonContext.java](https://github.com/DarkbladeDev/MultiBlockEngine/blob/main/src/main/java/com/darkbladedev/engine/api/addon/AddonContext.java)
- Implementación: [SimpleAddonContext.java](https://github.com/DarkbladeDev/MultiBlockEngine/blob/main/src/main/java/com/darkbladedev/engine/addon/SimpleAddonContext.java)
- Registry: [AddonServiceRegistry.java](https://github.com/DarkbladeDev/MultiBlockEngine/blob/main/src/main/java/com/darkbladedev/engine/addon/AddonServiceRegistry.java)

## Reglas importantes

1) `getService(...)` devuelve `null` si:

- nadie registró ese service
- el addon proveedor no está en estado `ENABLED`
- el tipo (`Class`) no coincide exactamente

2) Un service por tipo.

- Si otro addon intenta registrar el mismo `serviceType` con un proveedor distinto, el core lanza `IllegalStateException`.

3) El tipo del service (la `Class` usada como clave) debe ser compartido.

El registry indexa por `Class<?>`. Eso significa que proveedor y consumidor deben referenciar la MISMA clase cargada por el mismo classloader “visible”. En la práctica:

- evita definir la interfaz del service duplicada dentro de cada addon (aunque tenga el mismo package/nombre)
- usa un tipo que exista en el classloader del core (por ejemplo, una interfaz/clase publicada por el API del engine)

Si proveedor y consumidor compilan “la misma” interfaz pero cada uno la incluye dentro de su propio JAR, en runtime serán clases distintas y `getService()` nunca la resolverá.

## Cuándo registrar y cuándo obtener

Proveedor:

- recomendado: registrar en `onLoad(ctx)` si el service está disponible desde el inicio
- alternativa: registrar en `onEnable()` si depende de recursos que solo existen tras el enable

Consumidor:

- recomendado: obtener en `onEnable()`
- evita depender de services en `onLoad(...)` porque el proveedor puede no estar habilitado todavía

El core carga addons y luego los habilita en orden resuelto por dependencias. Referencia del flujo: [AddonManager.java](https://github.com/DarkbladeDev/MultiBlockEngine/blob/main/src/main/java/com/darkbladedev/engine/addon/AddonManager.java)

## Declarar dependencias en addon.properties

Declarar dependencias controla compatibilidad y orden.

```properties
depends.required=core:energy>=1.2.0
depends.optional=core:placeholders>=2.0.0
```

- `depends.required`: si falta o la versión es menor, tu addon falla.
- `depends.optional`: si falta o no cumple, el core registra un warning y tu addon debe desactivar esa integración.

Resolver: [AddonDependencyResolver.java](https://github.com/DarkbladeDev/MultiBlockEngine/blob/main/src/main/java/com/darkbladedev/engine/addon/AddonDependencyResolver.java)

## Ejemplo: addon proveedor

Supongamos que existe una interfaz `EnergyApi` visible para ambos addons.

```java
import com.darkbladedev.engine.api.addon.AddonContext;
import com.darkbladedev.engine.api.addon.AddonException;
import com.darkbladedev.engine.api.addon.MultiblockAddon;

public final class EnergyAddon implements MultiblockAddon {

    @Override
    public String getId() {
        return "core:energy";
    }

    @Override
    public String getVersion() {
        return "1.2.0";
    }

    @Override
    public void onLoad(AddonContext ctx) throws AddonException {
        ctx.registerService(EnergyApi.class, new EnergyApiImpl(ctx));
    }

    @Override
    public void onEnable() throws AddonException {
    }

    @Override
    public void onDisable() {
    }
}
```

## Ejemplo: addon consumidor

```java
import com.darkbladedev.engine.api.addon.AddonContext;
import com.darkbladedev.engine.api.addon.AddonException;
import com.darkbladedev.engine.api.addon.MultiblockAddon;

public final class MachinesAddon implements MultiblockAddon {

    private AddonContext ctx;

    @Override
    public String getId() {
        return "myaddons:machines";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public void onLoad(AddonContext ctx) throws AddonException {
        this.ctx = ctx;
    }

    @Override
    public void onEnable() throws AddonException {
        EnergyApi energy = ctx.getService(EnergyApi.class);
        if (energy == null) {
            ctx.getLogger().info("EnergyApi no disponible; integración deshabilitada");
            return;
        }

        energy.registerMachineType("basic_miner");
    }

    @Override
    public void onDisable() {
    }
}
```

## Buenas prácticas

- Mantén los services como contratos estables (API), no como clases internas.
- Para integraciones opcionales: usa `depends.optional` y maneja `null` en `getService()`.
- Para integraciones obligatorias: usa `depends.required` y consume el service en `onEnable()`.
- Evita registrar servicios que dependan de recursos que puedan fallar silenciosamente; si el service no puede inicializarse, falla en `onLoad` con `AddonException`.

## Troubleshooting rápido

### `getService(...)` siempre devuelve null

- El proveedor no está ENABLED (revisa logs del core).
- Estás llamando `getService` demasiado temprano (mueve a `onEnable`).
- El `serviceType` no es exactamente el mismo `Class` en ambos (clase duplicada/shaded en cada addon).

### "Service already registered"

- Dos addons intentaron registrar el mismo `serviceType`.
- Revisa que solo exista un proveedor para ese contrato, o usa un service distinto.

## Catálogo de services: MBE-UI

MBE-UI registra sus servicios en `onLoad(ctx)`.

Servicios registrados:

- `com.mbe.ui.api.services.UiMenuController`: abrir/refresh/cerrar menús por `menuId`.
- `com.mbe.ui.api.services.UiSessionService`: lectura de `sessionData` por jugador.
- `com.mbe.ui.api.services.UiPlaceholderService`: procesamiento de texto con placeholders.
- `com.mbe.ui.api.services.ux.UiUxMenusService`: directorio, reload, guardar YAML y abrir menús.

### Ejemplo: consumir `UiUxMenusService`

```java
import com.darkbladedev.engine.api.addon.AddonContext;
import com.mbe.ui.api.services.ux.UiUxMenusService;

public final class MyAddon {
    private AddonContext ctx;
    private org.bukkit.entity.Player player;

    public void onLoad(AddonContext ctx) {
        this.ctx = ctx;
    }

    public void onEnable() {
        UiUxMenusService menus = ctx.getService(UiUxMenusService.class);
        if (menus == null) {
            ctx.getLogger().warning("MBE-UI no disponible; integración deshabilitada");
            return;
        }

        menus.open(player, "example:main", java.util.Map.of(), java.util.Optional.empty());
    }
}
```

### Implementación en MBE-UI

- Registro: [MbeUiAddon.java](src/main/java/com/mbe/addons/ui/addon/MbeUiAddon.java)
- Instalador: [UiServiceInstaller.java](src/main/java/com/mbe/addons/ui/services/UiServiceInstaller.java)
