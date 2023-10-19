package wraith.alloyforgery.utils;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;

public class DataPackEvents {

    public static final Event<BeforeSync> BEFORE_SYNC = EventFactory.createArrayBacked(BeforeSync.class, callbacks -> (server) -> {
        for (BeforeSync callback : callbacks) {
            callback.beforeSync(server);
        }
    });

    public interface BeforeSync {
        void beforeSync(MinecraftServer server);
    }
}
