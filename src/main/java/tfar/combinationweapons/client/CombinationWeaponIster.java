package tfar.combinationweapons.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import tfar.combinationweapons.CombinationWeaponItem;

import java.util.Set;

public class CombinationWeaponIster extends ItemStackTileEntityRenderer {


    @Override
    public void func_239207_a_(ItemStack stack, ItemCameraTransforms.TransformType transformType, MatrixStack matrices, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay) {
        if (stack.hasTag()) {
            Set<Item> comboItems = CombinationWeaponItem.getCombination(stack);
            if (!comboItems.isEmpty()) {
                for (Item item : comboItems) {
                    matrices.push();
                    matrices.translate(.5,.5,.5);
                    ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
                    itemRenderer.renderItem(new ItemStack(item),transformType,combinedLight,combinedOverlay,matrices,buffer);
                    matrices.pop();
                }
            }
        }
    }
}
