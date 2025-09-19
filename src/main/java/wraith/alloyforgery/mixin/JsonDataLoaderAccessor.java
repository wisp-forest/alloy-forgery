package wraith.alloyforgery.mixin;

import com.mojang.serialization.Codec;
import net.minecraft.resource.JsonDataLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(JsonDataLoader.class)
public interface JsonDataLoaderAccessor<T> {
    @Accessor("codec")
    Codec<T> codec();
}
