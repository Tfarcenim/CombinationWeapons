package tfar.combinationweapons;

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import tfar.combinationweapons.client.CombinationWeaponIster;
import tfar.combinationweapons.client.WeaponProjectileRenderer;
import tfar.combinationweapons.entity.WeaponProjectileEntity;
import tfar.combinationweapons.recipe.CombinationWeaponRecipe;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(CombinationWeapons.MODID)
public class CombinationWeapons
{
    // Directly reference a log4j logger.

    public static final String MODID = "combinationweapons";

    public static final Item COMBINATION_WEAPON = new CombinationWeaponItem(new Item.Properties().group(ItemGroup.COMBAT).setISTER(() -> CombinationWeaponIster::new));

    public static final EntityType<WeaponProjectileEntity> ENTITY = EntityType.Builder.<WeaponProjectileEntity>create(WeaponProjectileEntity::new, EntityClassification.MISC)
            .size(.5f,.5f)
            .trackingRange(4).updateInterval(20)
            .build("combination_weapon");

    public static final SpecialRecipeSerializer<CombinationWeaponRecipe> RECIPE_SERIALIZER = new SpecialRecipeSerializer<>(CombinationWeaponRecipe::new);

    public CombinationWeapons() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        // Register the setup method for modloading
        bus.addListener(this::setup);
        // Register the doClientStuff method for modloading
        bus.addListener(this::doClientStuff);
        bus.addGenericListener(Item.class,this::item);
        bus.addGenericListener(IRecipeSerializer.class,this::recipe);
        bus.addGenericListener(EntityType.class,this::entity);
    }

    private void item(RegistryEvent.Register<Item> e) {
        e.getRegistry().register(COMBINATION_WEAPON.setRegistryName("combination_weapon"));
    }

    private void entity(RegistryEvent.Register<EntityType<?>> e) {
        e.getRegistry().register(ENTITY.setRegistryName("combination_weapon_projectile"));
    }

    private void recipe(RegistryEvent.Register<IRecipeSerializer<?>> e) {
        e.getRegistry().register(RECIPE_SERIALIZER.setRegistryName("combination_weapon"));
    }

    private void setup(final FMLCommonSetupEvent event) {
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(ENTITY, WeaponProjectileRenderer::new);
    }
}
