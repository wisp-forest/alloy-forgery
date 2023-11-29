package wraith.alloyforgery.utils;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;

public class DataPackEvents {

    /**
     * Called before the Minecraft Server is about to sync tags and recipes to players. Similar to
     * {@link ServerLifecycleEvents#SYNC_DATA_PACK_CONTENTS} as such is but only once at the head
     * of {@link PlayerManager#onDataPacksReloaded()}
     */
    public static final Event<BeforeSync> BEFORE_SYNC = EventFactory.createArrayBacked(BeforeSync.class, callbacks -> (server) -> {
        for (BeforeSync callback : callbacks) {
            callback.beforeSync(server);
        }
    });

    public interface BeforeSync {
        /**
         * Called right before tags and recipes are sent to players
         * when {@link PlayerManager#onDataPacksReloaded()} method
         * is invoked due to a /reload command call
         *
         * <p>For example, this event can be used to sync data loaded with custom resource reloaders.
         *
         * @param server The server
         */
        void beforeSync(MinecraftServer server);
    }
}
