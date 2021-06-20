package tfar.combinationweapons.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class WeaponProjectileEntity extends AbstractArrowEntity {

    private static final DataParameter<ItemStack> ITEM = EntityDataManager.createKey(WeaponProjectileEntity.class, DataSerializers.ITEMSTACK);

    public WeaponProjectileEntity(EntityType<? extends WeaponProjectileEntity> type, World worldIn) {
        super(type, worldIn);
    }

    protected WeaponProjectileEntity(EntityType<? extends WeaponProjectileEntity> type, double x, double y, double z, World worldIn) {
        super(type, x, y, z, worldIn);
    }

    public WeaponProjectileEntity(EntityType<? extends WeaponProjectileEntity> type, LivingEntity shooter, World worldIn,ItemStack stack) {
        super(type, shooter, worldIn);
        setItemStack(stack);
    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    protected void registerData() {
        super.registerData();
        this.getDataManager().register(ITEM, ItemStack.EMPTY);
    }

    /**
     * Gets the item that this entity represents.
     */
    public ItemStack getItemStack() {
        return this.getDataManager().get(ITEM);
    }

    /**
     * Sets the item that this entity represents.
     */
    public void setItemStack(ItemStack stack) {
        this.getDataManager().set(ITEM, stack);
    }

    public void notifyDataManagerChange(DataParameter<?> key) {
        super.notifyDataManagerChange(key);
        if (ITEM.equals(key)) {
            this.getItemStack().setAttachedEntity(this);
        }
    }

    public void writeAdditional(CompoundNBT compound) {
        super.writeAdditional(compound);

        if (!this.getItemStack().isEmpty()) {
            compound.put("Item", this.getItemStack().write(new CompoundNBT()));
        }

    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    public void readAdditional(CompoundNBT compound) {

        CompoundNBT compoundnbt = compound.getCompound("Item");
        this.setItemStack(ItemStack.read(compoundnbt));
        if (this.getItemStack().isEmpty()) {
            this.remove();
        }

    }


    @Override
    protected ItemStack getArrowStack() {
        return ItemStack.EMPTY;
    }
}
