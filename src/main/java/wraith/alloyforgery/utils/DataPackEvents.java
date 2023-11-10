package wraith.alloyforgery.utils;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;

public class DataPackEvents {

    /**
     * Event Similar to {@link ServerLifecycleEvents#SYNC_DATA_PACK_CONTENTS} with the goal as a
     * method to adjust Data Pack based data before sync after such has loaded
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
         * @param server server The server
         */
        void beforeSync(MinecraftServer server);
    }
}
