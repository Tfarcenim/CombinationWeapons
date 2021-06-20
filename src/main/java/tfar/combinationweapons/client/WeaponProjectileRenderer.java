package tfar.combinationweapons.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import tfar.combinationweapons.entity.WeaponProjectileEntity;

public class WeaponProjectileRenderer extends EntityRenderer<WeaponProjectileEntity> {
    public WeaponProjectileRenderer(EntityRendererManager renderManagerIn) {
        super(renderManagerIn);
    }

    public void render(WeaponProjectileEntity entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
        matrixStackIn.push();
        matrixStackIn.rotate(Vector3f.YP.rotationDegrees(MathHelper.lerp(partialTicks, entityIn.prevRotationYaw, entityIn.rotationYaw) - 90.0F));
        matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(MathHelper.lerp(partialTicks, entityIn.prevRotationPitch, entityIn.rotationPitch)- 45));

        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

        ItemStack thrown = entityIn.getItemStack();

        IBakedModel ibakedmodel = itemRenderer.getItemModelWithOverrides(thrown, entityIn.world, null);

        itemRenderer.renderItem(thrown, ItemCameraTransforms.TransformType.GROUND, false, matrixStackIn, bufferIn, packedLightIn, OverlayTexture.NO_OVERLAY, ibakedmodel);

        matrixStackIn.pop();
        super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
    }

    @Override
    public ResourceLocation getEntityTexture(WeaponProjectileEntity entity) {
        return PlayerContainer.LOCATION_BLOCKS_TEXTURE;
    }


}
