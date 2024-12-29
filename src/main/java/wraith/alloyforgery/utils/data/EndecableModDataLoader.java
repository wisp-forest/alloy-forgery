package wraith.alloyforgery.utils.data;

import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import io.wispforest.endec.Endec;
import io.wispforest.owo.moddata.ModDataConsumer;
import io.wispforest.owo.moddata.ModDataLoader;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;

import java.util.function.BiConsumer;

public class EndecableModDataLoader implements ModDataConsumer {

    private static final Logger LOGGER = LogUtils.getLogger();

    private final Identifier id;

    private final String subdirectory;
    private final EndecedHandler<?> handler;

    private EndecableModDataLoader(Identifier id, String subdirectory, EndecedHandler<?> handler) {
        this.id = id;
        this.subdirectory = subdirectory;
        this.handler = handler;
    }

    public static <T> EndecableModDataLoader of(Identifier id, String dataType, Endec<T> endec, BiConsumer<Identifier, T> consumer) {
        return new EndecableModDataLoader(id, dataType, new EndecedHandler<>(endec, consumer));
    }

    public static <T> EndecableModDataLoader of(Identifier id, String dataType, String fieldName, Endec<T> endec, BiConsumer<Identifier, T> consumer) {
        return new EndecableModDataLoader(id, dataType,
                EndecedHandler.of(fieldName, endec, consumer, entryId -> {
                    LOGGER.warn("A given entry within the [{}] Data Loader was found to be missing any data! [EntryId: {}]", id, entryId);
                }));
    }

    public void load() {
        ModDataLoader.load(this);
    }

    @Override
    public String getDataSubdirectory() {
        return subdirectory;
    }

    @Override
    public void acceptParsedFile(Identifier id, JsonObject object) {
        handler.handle(id, object);
    }
}
