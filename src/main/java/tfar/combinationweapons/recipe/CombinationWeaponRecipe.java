package tfar.combinationweapons.recipe;

import com.google.common.collect.Lists;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.*;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import tfar.combinationweapons.CombinationWeaponItem;
import tfar.combinationweapons.CombinationWeapons;

import java.util.List;

public class CombinationWeaponRecipe extends SpecialRecipe {
    public CombinationWeaponRecipe(ResourceLocation idIn) {
        super(idIn);
    }

    @Override
    public boolean matches(CraftingInventory inv, World worldIn) {
        List<ItemStack> list = Lists.newArrayList();

        for (int i = 0; i < inv.getSizeInventory(); ++i) {
            ItemStack stack1 = inv.getStackInSlot(i);
            if (!stack1.isEmpty()) {
                list.add(stack1);
                if (list.size() > 1) {
                    ItemStack stack0 = list.get(0);
                    if (!canCombine(stack0, stack1)) {
                        return false;
                    }
                }
            }
        }
        return list.size() == 2;
    }

    private static boolean canCombine(ItemStack stack0, ItemStack stack1) {
        Item item0 = stack0.getItem();
        Item item1 = stack1.getItem();

        return item0.getClass() != item1.getClass() && combinable(item0) && combinable(item1);
    }

    private static boolean combinable(Item item) {
        return item instanceof SwordItem || item instanceof BowItem || item instanceof AxeItem || item instanceof PickaxeItem || item instanceof ShovelItem;
    }

    @Override
    public ItemStack getCraftingResult(CraftingInventory inv) {

        ItemStack weapon = new ItemStack(CombinationWeapons.COMBINATION_WEAPON);


        List<ItemStack> list = Lists.newArrayList();

        for (int i = 0; i < inv.getSizeInventory(); ++i) {
            ItemStack stack = inv.getStackInSlot(i);
            if (!stack.isEmpty()) {
                list.add(stack);
                if (list.size() > 1) {
                    ItemStack stack1 = list.get(0);
                    if (!canCombine(stack, stack1)) {
                        return ItemStack.EMPTY;
                    }
                }
            }
        }

        if (list.size() == 2) {
            ItemStack stack0 = list.get(0);
            ItemStack stack1 = list.get(1);
            if (canCombine(stack0, stack1)) {

                CombinationWeaponItem.putCombination(weapon,list);

                return weapon;
            }
        }

        return ItemStack.EMPTY;
    }

    @Override
    public boolean canFit(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return CombinationWeapons.RECIPE_SERIALIZER;
    }
}
