package wraith.alloyforgery.client;

import com.glisco.owo.particles.ClientParticles;
import com.glisco.owo.particles.ServerParticles;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import wraith.alloyforgery.AlloyForgery;

@Environment(EnvType.CLIENT)
public class AlloyForgeryClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ScreenRegistry.register(AlloyForgery.ALLOY_FORGE_SCREEN_HANDLER_TYPE, AlloyForgeScreen::new);

        ServerParticles.registerClientSideHandler(AlloyForgery.id("smelting_particles"), (client, pos, data) -> {
            final Direction facing = Direction.values()[data.readVarInt()];

            client.execute(() -> {
                final Vec3d particleSide = Vec3d.of(pos).add(0.5 + facing.getOffsetX() * 0.515, 0.25, 0.5 + facing.getOffsetZ() * 0.515);
                ClientParticles.spawnPrecise(ParticleTypes.FLAME, client.world, particleSide, facing.getOffsetZ() * 0.65, 0.175, facing.getOffsetX() * 0.65);
                ClientParticles.spawnPrecise(ParticleTypes.SMOKE, client.world, particleSide, facing.getOffsetZ() * 0.65, 0.175, facing.getOffsetX() * 0.65);
            });
        });
    }

}
