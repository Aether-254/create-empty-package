package awa.Aether_254.create_empty_package.mixin;

import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import com.simibubi.create.content.logistics.packager.PackagerItemHandler;
import com.simibubi.create.content.logistics.packager.PackagingRequest;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.InvManipulationBehaviour;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PackagerBlockEntity.class)
abstract class PackagerBlockEntityMixin {
    @Shadow(remap = false) public ItemStack heldBox;
    @Shadow(remap = false) public int animationTicks;
    @Shadow(remap = false) public int buttonCooldown;
    @Shadow(remap = false) public String signBasedAddress;
    @Shadow(remap = false) public boolean animationInward;
    @Shadow(remap = false) public InvManipulationBehaviour targetInventory;

    @Shadow(remap = false) public abstract void triggerStockCheck();
    @Shadow(remap = false) public abstract void notifyUpdate();

    @Inject(method = "attemptToSend(Ljava/util/List;)V", at = @At("HEAD"), cancellable = true, remap = false)
    private void createEmptyPackage$packageEmptyTarget(List<PackagingRequest> requests, CallbackInfo ci) {
        if (requests != null)
            return;
        if (!heldBox.isEmpty() || animationTicks != 0 || buttonCooldown > 0)
            return;

        IItemHandler target = targetInventory.getInventory();
        if (target instanceof PackagerItemHandler || hasItems(target))
            return;

        ItemStack box = PackageItem.containing(new ItemStackHandler(PackageItem.SLOTS));
        if (!signBasedAddress.isBlank())
            PackageItem.addAddress(box, signBasedAddress);

        heldBox = box;
        animationInward = false;
        animationTicks = PackagerBlockEntity.CYCLE;
        triggerStockCheck();
        notifyUpdate();
        ci.cancel();
    }

    private static boolean hasItems(IItemHandler target) {
        if (target == null)
            return false;
        for (int slot = 0; slot < target.getSlots(); slot++) {
            if (!target.getStackInSlot(slot).isEmpty())
                return true;
        }
        return false;
    }
}
