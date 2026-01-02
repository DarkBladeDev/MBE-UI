package com.mbe.addons.ui.core;

import com.darkbladedev.engine.api.addon.AddonContext;
import com.darkbladedev.engine.api.logging.EngineLogger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.nio.file.Path;

public final class CoreServiceRegistry {
    private static final AtomicBoolean REGISTERED = new AtomicBoolean(false);

    private CoreServiceRegistry() {
    }

    public static boolean registerOnce(AddonContext ctx, Runnable registration) {
        Objects.requireNonNull(ctx, "ctx");
        Objects.requireNonNull(registration, "registration");

        Method registerService = resolveRegisterService(ctx);
        if (registerService == null) {
            ctx.getLogger().warn("Core service registry not available; skipping service registration");
            return false;
        }

        if (!REGISTERED.compareAndSet(false, true)) {
            return false;
        }

        registration.run();
        return true;
    }

    public static void register(AddonContext ctx, Class<?> serviceType, Object service) {
        Objects.requireNonNull(ctx, "ctx");
        Objects.requireNonNull(serviceType, "serviceType");
        Objects.requireNonNull(service, "service");

        Method registerService = resolveRegisterService(ctx);
        if (registerService == null) {
            throw new IllegalStateException("Core service registry not available");
        }

        try {
            registerService.invoke(ctx, serviceType, service);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot access AddonContext.registerService", e);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause() == null ? e : e.getCause();
            throw new IllegalStateException("AddonContext.registerService failed: " + cause.getClass().getSimpleName(), cause);
        }
    }

    public static EngineLogger logger(AddonContext ctx) {
        Objects.requireNonNull(ctx, "ctx");
        return ctx.getLogger();
    }

    public static Path dataFolder(AddonContext ctx) {
        Objects.requireNonNull(ctx, "ctx");
        Object candidate = invokeNoArg(ctx, "getDataFolder", "dataFolder");
        if (candidate instanceof Path p) {
            return p;
        }
        return Path.of(".");
    }

    public static void runTask(AddonContext ctx, Runnable task) {
        Objects.requireNonNull(ctx, "ctx");
        Objects.requireNonNull(task, "task");
        if (invokeRunnable(ctx, task, "runTask", "runSync")) {
            return;
        }
        task.run();
    }

    public static void runTaskAsync(AddonContext ctx, Runnable task) {
        Objects.requireNonNull(ctx, "ctx");
        Objects.requireNonNull(task, "task");
        if (invokeRunnable(ctx, task, "runTaskAsync", "runAsync", "runAsyncTask")) {
            return;
        }
        new Thread(task, "MBE-UI-async").start();
    }

    private static Method resolveRegisterService(AddonContext ctx) {
        for (Method m : ctx.getClass().getMethods()) {
            if (!m.getName().equals("registerService")) {
                continue;
            }
            if (m.getParameterCount() != 2) {
                continue;
            }
            Class<?>[] p = m.getParameterTypes();
            if (p[0] == Class.class) {
                return m;
            }
        }
        return null;
    }

    private static Object invokeNoArg(Object target, String... names) {
        for (String name : names) {
            try {
                Method m = target.getClass().getMethod(name);
                return m.invoke(target);
            } catch (NoSuchMethodException ignored) {
            } catch (IllegalAccessException | InvocationTargetException ignored) {
            } catch (Throwable ignored) {
            }
        }
        return null;
    }

    private static boolean invokeRunnable(Object target, Runnable task, String... names) {
        for (String name : names) {
            try {
                for (Method m : target.getClass().getMethods()) {
                    if (!m.getName().equals(name)) {
                        continue;
                    }
                    if (m.getParameterCount() != 1) {
                        continue;
                    }
                    if (!m.getParameterTypes()[0].isAssignableFrom(Runnable.class) && !Runnable.class.isAssignableFrom(m.getParameterTypes()[0])) {
                        continue;
                    }
                    m.invoke(target, task);
                    return true;
                }
            } catch (IllegalAccessException | InvocationTargetException ignored) {
            } catch (Throwable ignored) {
            }
        }
        return false;
    }
}
