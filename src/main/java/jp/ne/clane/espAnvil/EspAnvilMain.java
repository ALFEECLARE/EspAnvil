package jp.ne.clane.espAnvil;

import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(EspAnvilMain.MOD_ID)
public class EspAnvilMain {
	public static final String MOD_ID = "espanvil";
	public static final String MOD_NAME = "EspAnvil";
	public static final String[] MOD_AUTHORS = {"ALFEECLARE@CLANE SOFTWARE"};
	private static final Logger log = LogManager.getLogger(MOD_ID);
	public static Minecraft mc;

	private static EspAnvilMain instance;

	private EspAnvilConfig config;

	private static void log(String message) {
		log.info("[{}] {}", log.getName(), message);
	}

	/**
	 * Reload modules
	 */
	public EspAnvilMain modules() {
		try {
			mc = Minecraft.getInstance();
			if (mc.levelRenderer != null)
					mc.levelRenderer.allChanged();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
		return this;
	}

	public EspAnvilMain() {
		instance = this;
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		bus.addListener(this::setup);

		MinecraftForge.EVENT_BUS.register(this);
		log("espAnvil Started");
	}

	/**
	 * get this mod
	 */
	public static EspAnvilMain getMod() {
		return instance;
	}

	@SubscribeEvent
	public void ItemTooltipEvent(ItemTooltipEvent ev) {
		Minecraft mcInst = getMC();
		ItemStack items = ev.getItemStack();
		List<Component> tooltip = ev.getToolTip();
		if (items.isRepairable() || !EnchantedBookItem.getEnchantments(items).isEmpty()) {
			if (EspAnvilConfig.isShowAnvilCount) {
				tooltip.add(Component.translatable("clane.mod.espAnvil.anvilUseCount", Math.round(Math.log(items.getBaseRepairCost() + 1) / Math.log(2))));
			}
			if (EspAnvilConfig.isShowItemDurability && !ev.getFlags().isAdvanced() && items.isRepairable()) {
				tooltip.add(Component.translatable("clane.mod.espAnvil.itemDurability", items.getMaxDamage() - items.getDamageValue(), items.getMaxDamage()));
			}
		} else if (items.isEdible()) {
			FoodProperties foodProp = items.getFoodProperties(null);
			FoodData currentFoodData = mcInst.player.getFoodData();
			int afterNutrationLevel = Math.min(20, currentFoodData.getFoodLevel() + foodProp.getNutrition());
			if (EspAnvilConfig.isShowNutrition) {
				MutableComponent addCompNut = Component.translatable("clane.mod.espAnvil.nutrition");
				addCompNut.append(Component.translatable("clane.mod.espAnvil.hungerValueAdd",foodProp.getNutrition()));
				if (EspAnvilConfig.isShowNutritionBeforeAfter) {
					addCompNut.append(Component.translatable("clane.mod.espAnvil.hungerValue.BeforeAfter", currentFoodData.getFoodLevel(), afterNutrationLevel));
				}
				tooltip.add(addCompNut);
			}
			if (EspAnvilConfig.isShowSaturation) {
				Float addSaturationValue = (float)foodProp.getNutrition() * foodProp.getSaturationModifier() * 2.0F;
				MutableComponent addCompSat = Component.translatable("clane.mod.espAnvil.saturation");
				addCompSat.append(Component.translatable("clane.mod.espAnvil.hungerValueAdd",String.format("%.1f",addSaturationValue)));
				if (EspAnvilConfig.isShowSaturationBeforeAfter) {
					addCompSat.append(Component.translatable("clane.mod.espAnvil.hungerValue.BeforeAfter", String.format("%.1f",currentFoodData.getSaturationLevel()), String.format("%.1f",Math.min(afterNutrationLevel, currentFoodData.getSaturationLevel() + addSaturationValue))));
				}
				tooltip.add(addCompSat);
			}
			if (EspAnvilConfig.isShowFoodExtraInfo) {
				StringBuilder optionValue = new StringBuilder();
				String optionSeparater = Component.translatable("clane.mod.espAnvil.food.optionSeparater").getString();
				if (foodProp.isFastFood()) {
					optionValue.append(Component.translatable("clane.mod.espAnvil.food.fastfood").getString()).append(optionSeparater);
				}
				if (foodProp.canAlwaysEat()) {
					optionValue.append(Component.translatable("clane.mod.espAnvil.food.alwayseat").getString()).append(optionSeparater);
				}
				if (optionValue.length() > 0) {
					tooltip.add(Component.literal(optionValue.delete(optionValue.length() - optionSeparater.length(), optionValue.length()).toString()));
				}
			}
		} else if (items.getItem() instanceof BlockItem) {
			Block block = ((BlockItem)items.getItem()).getBlock();;
			if (EspAnvilConfig.isShowBlockDestroyTime) {
				float destroyTime = block.defaultDestroyTime();
				tooltip.add(Component.translatable("clane.mod.espAnvil.block.destroyTime", destroyTime == 0 ? Component.translatable("clane.mod.espAnvil.block.instantBreak") : destroyTime == -1 ? Component.translatable("clane.mod.espAnvil.block.unbreakable") : String.format("%.1f",destroyTime)));
			}
			if (EspAnvilConfig.isShowExplosionResistance) {
				float explosionResistance = block.getExplosionResistance(block.defaultBlockState(), null, null, null);
				tooltip.add(Component.translatable("clane.mod.espAnvil.block.explosionResistance", String.format("%.1f",explosionResistance)));
			}
			if (EspAnvilConfig.isShowLightLevel) {
				int lightLevel = block.getLightEmission(block.defaultBlockState(), mcInst.level, null);
				if (lightLevel > 0) {
					tooltip.add(Component.translatable("clane.mod.espAnvil.block.lightLevel", lightLevel));
				}
			}
		}
	}

	private void setup(final FMLCommonSetupEvent event) {
		config = new EspAnvilConfig();
		try {
			config.loadConfig(EspAnvilConfig.getConfigFile());
		} catch (IllegalAccessException | IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		try {
			config.saveConfig(EspAnvilConfig.getConfigFile());
		} catch (IllegalAccessException | IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		log("espAnvil Init");
	}
	
	private Minecraft getMC() {
		if (mc == null) {	
			try {
				mc = Minecraft.getInstance();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			}
		}
		return mc;
	}
	
	
}
