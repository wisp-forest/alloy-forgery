package io.wispforest.alloyforgery.neoforge.api;

import net.neoforged.neoforge.transfer.resource.Resource;

public record LongResourceStack<T extends Resource>(T resource, long amount) { }
