package io.wispforest.alloyforgery.neoforge.utils;

import net.neoforged.neoforgespi.language.IModInfo;

import java.nio.file.Path;

public class CursedLoaderHacks {
    public static Path getPath(IModInfo container) {
        return container.getOwningFile().getFile().getSecureJar().getRootPath();
    }
}
