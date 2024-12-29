package wraith.alloyforgery.networking;

import io.wispforest.endec.Endec;
import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.network.ServerAccess;
import net.minecraft.block.entity.BlockEntity;
import wraith.alloyforgery.AlloyForgery;
import wraith.alloyforgery.client.BlockEntityLocation;

import java.util.Map;

public record DisableSlotToggle(BlockEntityLocation location, Integer slotIndex, Boolean isDisabled) {

    public static final StructEndec<DisableSlotToggle> ENDEC = StructEndecBuilder.of(
            BlockEntityLocation.ENDEC.fieldOf("location", DisableSlotToggle::location),
            Endec.VAR_INT.fieldOf("slot_index", DisableSlotToggle::slotIndex),
            Endec.BOOLEAN.fieldOf("is_disabled", DisableSlotToggle::isDisabled),
            DisableSlotToggle::new
    );

    public DisableSlotToggle(BlockEntity blockEntity, Integer slotIndex, Boolean state){
        this(BlockEntityLocation.of(blockEntity), slotIndex, state);
    }

    public static void handle(DisableSlotToggle packet, ServerAccess serverAccess) {
        var forge = packet.location().get(serverAccess.player(), AlloyForgery.FORGE_CONTROLLER_BLOCK_ENTITY);

        if (packet.isDisabled()) {
            forge.disableSlot(packet.slotIndex());
        } else {
            forge.enableSlot(packet.slotIndex());
        }
    }
}
