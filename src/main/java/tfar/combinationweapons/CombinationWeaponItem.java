package tfar.combinationweapons;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.block.BlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.stats.Stats;
import net.minecraft.util.*;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.util.Constants;
import tfar.combinationweapons.entity.WeaponProjectileEntity;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CombinationWeaponItem extends Item {
    public CombinationWeaponItem(Properties properties) {
        super(properties);
    }

    public static Set<Item> getCombination(ItemStack weapon) {
        HashSet<Item> items = new HashSet<>();
        CompoundNBT nbt = weapon.getTag();
        if (nbt != null) {
            ListNBT listNBT = nbt.getList(CombinationWeapons.MODID, Constants.NBT.TAG_STRING);
            for (INBT inbt : listNBT) {
                StringNBT stringNBT = (StringNBT) inbt;
                Item item = Registry.ITEM.getOrDefault(new ResourceLocation(stringNBT.getString()));
                items.add(item);
            }
        }
        return items;
    }

    @Override
    public boolean canHarvestBlock(ItemStack stack, BlockState state) {
        for (Item item : getCombination(stack)) {
            if (item.canHarvestBlock(state)) {
                return true;
            }
        }
        return super.canHarvestBlock(stack, state);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        for (Item item : getCombination(stack)) {
            float speed = item.getDestroySpeed(new ItemStack(item),state);
            if (speed > 1) {
                return speed;
            }
        }
        return super.getDestroySpeed(stack, state);
    }

    @Override
    public boolean canDisableShield(ItemStack stack, ItemStack shield, LivingEntity entity, LivingEntity attacker) {
        return hasAxe(stack);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        Set<Item> items = getCombination(stack);
        if (!items.isEmpty()) {
            String s = "Combination of %s";
            String[] translations = items.stream().map(Item::getTranslationKey).map(s1 -> I18n.format(s1)).toArray(String[]::new);

            int count = items.size();
            for (int i =1; i < count;i++) {
                s += " and %s";
            }
            tooltip.add(new StringTextComponent(I18n.format(s,translations)).mergeStyle(TextFormatting.AQUA));
        }
    }

    /**
     * Called when the player stops using an Item (stops holding the right mouse button).
     */
    public void onPlayerStoppedUsing(ItemStack weapon, World worldIn, LivingEntity entityLiving, int timeLeft) {
        if (entityLiving instanceof PlayerEntity) {
            PlayerEntity playerentity = (PlayerEntity)entityLiving;
            boolean infiniteAmmo = true;//playerentity.abilities.isCreativeMode || EnchantmentHelper.getEnchantmentLevel(Enchantments.INFINITY, weapon) > 0;
            ItemStack ammo = playerentity.findAmmo(weapon);

            int i = this.getUseDuration(weapon) - timeLeft;
            i = net.minecraftforge.event.ForgeEventFactory.onArrowLoose(weapon, worldIn, playerentity, i, !ammo.isEmpty() || infiniteAmmo);
            if (i < 0) return;

            if (!ammo.isEmpty() || infiniteAmmo) {
                if (ammo.isEmpty()) {
                    ammo = new ItemStack(Items.ARROW);
                }

                float f = getArrowVelocity(i);
                if (!((double)f < 0.1D)) {
                    boolean flag1 = playerentity.abilities.isCreativeMode || (ammo.getItem() instanceof ArrowItem && ((ArrowItem)ammo.getItem()).isInfinite(ammo, weapon, playerentity));
                    if (!worldIn.isRemote) {

                        ItemStack thrown = new ItemStack(getNotBow(weapon));

                        AbstractArrowEntity abstractarrowentity = new WeaponProjectileEntity(CombinationWeapons.ENTITY, playerentity, worldIn,thrown);

                        abstractarrowentity.setDirectionAndMovement(playerentity, playerentity.rotationPitch, playerentity.rotationYaw, 0.0F, f * 3.0F, 1.0F);

                        if (f == 1.0F) {
                            abstractarrowentity.setIsCritical(true);
                        }

                        int j = EnchantmentHelper.getEnchantmentLevel(Enchantments.POWER, weapon);
                        if (j > 0) {
                            abstractarrowentity.setDamage(abstractarrowentity.getDamage() + (double)j * 0.5D + 0.5D);
                        }

                        int k = EnchantmentHelper.getEnchantmentLevel(Enchantments.PUNCH, weapon);
                        if (k > 0) {
                            abstractarrowentity.setKnockbackStrength(k);
                        }

                        if (EnchantmentHelper.getEnchantmentLevel(Enchantments.FLAME, weapon) > 0) {
                            abstractarrowentity.setFire(100);
                        }

                        weapon.damageItem(1, playerentity, (player) -> {
                            player.sendBreakAnimation(playerentity.getActiveHand());
                        });
                        if (flag1 || playerentity.abilities.isCreativeMode && (ammo.getItem() == Items.SPECTRAL_ARROW || ammo.getItem() == Items.TIPPED_ARROW)) {
                            abstractarrowentity.pickupStatus = AbstractArrowEntity.PickupStatus.CREATIVE_ONLY;
                        }

                        worldIn.addEntity(abstractarrowentity);
                    }

                    worldIn.playSound(null, playerentity.getPosX(), playerentity.getPosY(), playerentity.getPosZ(), SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.PLAYERS, 1.0F, 1.0F / (random.nextFloat() * 0.4F + 1.2F) + f * 0.5F);
                    if (!flag1 && !playerentity.abilities.isCreativeMode) {
                        ammo.shrink(1);
                        if (ammo.isEmpty()) {
                            playerentity.inventory.deleteStack(ammo);
                        }
                    }

                    playerentity.addStat(Stats.ITEM_USED.get(this));
                }
            }
        }
    }

    /**
     * Gets the velocity of the arrow entity from the bow's charge
     */
    public static float getArrowVelocity(int charge) {
        float f = (float)charge / 20.0F;
        f = (f * f + f * 2.0F) / 3.0F;
        if (f > 1.0F) {
            f = 1.0F;
        }

        return f;
    }

    /**
     * How long it takes to use or consume an item
     */
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    /**
     * returns the action that specifies what animation to play when the items is being used
     */
    public UseAction getUseAction(ItemStack stack) {
        if (isBow(stack)) {
            return UseAction.BOW;
        }
        return UseAction.NONE;
    }

    /**
     * Called to trigger the item's "innate" right click behavior. To handle when this item is used on a Block, see
     * {@link #onItemUse}.
     */
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        ItemStack itemstack = playerIn.getHeldItem(handIn);

        if (isBow(itemstack)) {

            boolean hasAmmo = true;//!playerIn.findAmmo(itemstack).isEmpty();

            ActionResult<ItemStack> ret = net.minecraftforge.event.ForgeEventFactory.onArrowNock(itemstack, worldIn, playerIn, handIn, hasAmmo);
            if (ret != null) return ret;

            if (!playerIn.abilities.isCreativeMode && !hasAmmo) {
                return ActionResult.resultFail(itemstack);
            } else {
                playerIn.setActiveHand(handIn);
                return ActionResult.resultConsume(itemstack);
            }
        } else {
            return ActionResult.resultPass(playerIn.getHeldItem(handIn));
        }
    }

    @Override
    public Set<ToolType> getToolTypes(ItemStack stack) {
        Set<ToolType> toolTypes = new HashSet<>();
        if (hasAxe(stack)) {
            toolTypes.add(ToolType.AXE);
        }
        return toolTypes;
    }


    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlotType slot, ItemStack stack) {
        if (slot == EquipmentSlotType.MAINHAND) {
            ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
            builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", getAttackDamage(stack), AttributeModifier.Operation.ADDITION));
            //builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", (double)attackSpeedIn, AttributeModifier.Operation.ADDITION));
            return builder.build();
        }
        return ImmutableMultimap.of();
    }

    public static float getAttackDamage(ItemStack weapon) {
        float damage = 0;
        for (Item item : getCombination(weapon)) {
            if (item instanceof SwordItem) {
                float damage0 = ((SwordItem) item).getAttackDamage()-1;
                if (damage0 > damage) damage = damage0;
            } else if (item instanceof ToolItem) {
                float damage0 = ((ToolItem) item).getAttackDamage()-1;
                if (damage0 > damage) damage = damage0;
            }
        }
        return damage;
    }

    public static boolean hasAxe(ItemStack weapon) {
        return getCombination(weapon).stream().anyMatch(item -> item instanceof AxeItem);
    }

    public static void putCombination(ItemStack weapon, Collection<ItemStack> items) {
        ListNBT listNBT = new ListNBT();
        for (ItemStack item : items) {
            String s = item.getItem().getRegistryName().toString();
            StringNBT stringNBT = StringNBT.valueOf(s);
            listNBT.add(stringNBT);
        }
        weapon.getOrCreateTag().put(CombinationWeapons.MODID,listNBT);
    }

    public static boolean isBow(ItemStack weapon) {
        return getCombination(weapon).stream().anyMatch(item -> item instanceof BowItem);
    }

    public static boolean isSword(ItemStack weapon) {
        return !isBow(weapon) && getCombination(weapon).stream().anyMatch(item -> item instanceof SwordItem);
    }

    public static Item getNotBow(ItemStack weapon) {
        return getCombination(weapon).stream().filter(item -> !(item instanceof ShootableItem)).findFirst().orElse(Items.AIR);
    }
}
