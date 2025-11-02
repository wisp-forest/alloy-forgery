package io.wispforest.alloyforgery.neoforge.utils;

import cpw.mods.jarhandling.impl.Jar;
import cpw.mods.jarhandling.impl.JarContentsImpl;
import cpw.mods.niofs.union.UnionFileSystem;
import io.wispforest.alloyforgery.utils.LoaderPlatformUtils;
import net.neoforged.neoforgespi.locating.IModFile;
import sun.misc.Unsafe;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;

/// Do not look to closely or your eyes may explode, as this will be removed in the future and only needed for development with Arch Projects
public class CursedLoaderHacks {

    private static Function<IModFile, List<Path>> pathGetter = modFile -> List.of(modFile.getFilePath());

    static {
        try {
            if (LoaderPlatformUtils.INSTANCE.isDevelopmentEnvironment()) {
                Unsafe UNSAFE = attemptToGetValue(() -> {
                    var theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
                    theUnsafe.setAccessible(true);
                    return theUnsafe.get(null);
                });
                MethodHandles.Lookup TRUSTED_LOOKUP = attemptToGetValue(() -> {
                    Field hackfield = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
                    return UNSAFE.getObject(UNSAFE.staticFieldBase(hackfield), UNSAFE.staticFieldOffset(hackfield));
                });
                VarHandle JAR_CONTENTS = attemptToGetValue(() -> TRUSTED_LOOKUP.findVarHandle(Jar.class, "contents", JarContentsImpl.class));
                VarHandle JAR_CONTENTS_FILE_SYSTEM = attemptToGetValue(() -> TRUSTED_LOOKUP.findVarHandle(JarContentsImpl.class, "filesystem", UnionFileSystem.class));
                VarHandle UNION_FILE_SYSTEM_BASE_PATHS = attemptToGetValue(() -> TRUSTED_LOOKUP.findVarHandle(UnionFileSystem.class, "basepaths", List.class));

                final var baseGetter = pathGetter;

                pathGetter = modFile -> {
                    if (modFile.getSecureJar() instanceof Jar jar) {
                        var jarContents = ((JarContentsImpl) JAR_CONTENTS.get(jar));

                        if (jarContents instanceof JarContentsImpl contents) {
                            var fileSystem = ((UnionFileSystem) JAR_CONTENTS_FILE_SYSTEM.get(contents));

                            return ((List<Path>) UNION_FILE_SYSTEM_BASE_PATHS.get(fileSystem));
                        }
                    }

                    return baseGetter.apply(modFile);
                };
            }
        } catch (Exception e) {
            throw new RuntimeException("CursedLoaderHacks ran into a issue initializing which means issues will be present in dev!", e);
        }
    }

    public static List<Path> getBasePaths(IModFile modFile) {
        return pathGetter.apply(modFile);
    }

    private static <R> R attemptToGetValue(SupplierWithException<Object> supplier) throws Exception {
        try {
            return (R) supplier.get();
        } catch (Exception exception) {
            throw exception;
        }
    }

    private interface SupplierWithException<T> {
        T get() throws Exception;
    }
}
