package tfar.combinationweapons.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfar.combinationweapons.CombinationWeaponItem;

@Mixin(PlayerEntity.class)
abstract class ExampleMixin extends LivingEntity {
	protected ExampleMixin(EntityType<? extends LivingEntity> type, World worldIn) {
		super(type, worldIn);
	}

	@ModifyVariable(at = @At("HEAD"), method = "attackTargetEntityWithCurrentItem",ordinal = 2,
	slice = @Slice(
			from = @At(value = "INVOKE",target = "Lnet/minecraft/item/ItemStack;getItem()Lnet/minecraft/item/Item;"),
			to = @At(value = "INVOKE",target = "Lnet/minecraft/entity/player/PlayerEntity;spawnSweepParticles()V")
	)
	)
	private boolean enableSweep(boolean old) {
		ItemStack itemstack = this.getHeldItem(Hand.MAIN_HAND);
		if (itemstack.getItem() instanceof CombinationWeaponItem && CombinationWeaponItem.isSword(itemstack)) {
			return true;
		}
		return old;
	}
}
