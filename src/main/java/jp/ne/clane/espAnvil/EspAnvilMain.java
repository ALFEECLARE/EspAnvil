package jp.ne.clane.espAnvil;

import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.datafixers.util.Pair;

import jp.ne.clane.commons.EffectUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CompassItem;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SuspiciousStewItem;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
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
				for (Pair<MobEffectInstance, Float> effect : foodProp.getEffects() ) {
					optionValue.append(EffectUtils.getEffectDescribeText(effect.getFirst(), effect.getSecond(), optionSeparater));
				}
				if (items.getItem() instanceof SuspiciousStewItem && !ev.getFlags().isCreative()) { //}!mcInst.player.isCreative()) {
					CompoundTag compoundtag = items.getTag();
					if (compoundtag != null && compoundtag.contains(SuspiciousStewItem.EFFECTS_TAG, Tag.TAG_LIST)) {
						ListTag listtag = compoundtag.getList(SuspiciousStewItem.EFFECTS_TAG, Tag.TAG_COMPOUND);
						for(int i = 0; i < listtag.size(); ++i) {
							CompoundTag compoundtag1 = listtag.getCompound(i);
							int j;
							if (compoundtag1.contains(SuspiciousStewItem.EFFECT_DURATION_TAG, Tag.TAG_ANY_NUMERIC)) {
								j = compoundtag1.getInt(SuspiciousStewItem.EFFECT_DURATION_TAG);
							} else {
								j = SuspiciousStewItem.DEFAULT_DURATION;
							}
							MobEffect mobeffect = MobEffect.byId(compoundtag1.getInt(SuspiciousStewItem.EFFECT_ID_TAG));
							if (mobeffect != null) {
								optionValue.append(EffectUtils.getEffectDescribeText(new MobEffectInstance(mobeffect, j), 1.0f, optionSeparater));
							}
						}
					}
				}
				if (optionValue.length() > 0) {
					tooltip.add(Component.literal(optionValue.delete(optionValue.length() - optionSeparater.length(), optionValue.length()).toString()));
				}
			}
		} else if (items.getItem() instanceof BlockItem) {
			Block block = ((BlockItem)items.getItem()).getBlock();
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
			if (EspAnvilConfig.isShowCorrectTool) {
				BlockState state = block.defaultBlockState();
				TagKey<Block> tier = filterBlockTag(state, BlockTags.NEEDS_DIAMOND_TOOL, BlockTags.NEEDS_IRON_TOOL, BlockTags.NEEDS_STONE_TOOL);
				TagKey<Block> tool = filterBlockTag(state, BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.MINEABLE_WITH_AXE, BlockTags.MINEABLE_WITH_SHOVEL, BlockTags.MINEABLE_WITH_HOE, BlockTags.SWORD_EFFICIENT);
				StringBuilder optionValue = new StringBuilder();
				optionValue.append(Component.translatable("clane.mod.espAnvil.block.requiredTool",Component.translatable("clane.mod.espAnvil.block.requiredTool." + convertNameFromBlockTagKey(tool)).getString()).getString());
				if (tier != null) {
					optionValue.append(Component.translatable("clane.mod.espAnvil.block.requiredTier",Component.translatable("clane.mod.espAnvil.block.requiredTier." + convertNameFromBlockTagKey(tier)).getString()).getString());
				}
				tooltip.add(Component.literal(optionValue.toString()));
			}
			if (EspAnvilConfig.isShowBlockExtraInfo) {
				if (block instanceof BeehiveBlock) {
					CompoundTag stateData = getItemBlockStateData(items);
					CompoundTag entityData = getItemBlockEntityData(items);
					tooltip.add(Component.translatable("clane.mod.espAnvil.block.beefiveBlock.honeyLevel", stateData == null ? 0 : stateData.getInt(BlockStateProperties.LEVEL_HONEY.getName()), BeehiveBlock.MAX_HONEY_LEVELS));
					tooltip.add(Component.translatable("clane.mod.espAnvil.block.beefiveBlock.beeCount",  entityData == null ? 0 : entityData.getList(BeehiveBlockEntity.BEES, Tag.TAG_COMPOUND).size(), BeehiveBlockEntity.MAX_OCCUPANTS));
				}
			}
		}	 else if (items.getItem() instanceof CompassItem) {
			if (CompassItem.isLodestoneCompass(items)) {
				GlobalPos gpos = CompassItem.getLodestonePosition(items.getTag());
				tooltip.add(Component.translatable("clane.mod.espAnvil.compass.target", Component.translatable(gpos.dimension().location().toLanguageKey()).getString(), gpos.pos().getX(), gpos.pos().getY(), gpos.pos().getZ()));
			}
		}
	}
	
	private CompoundTag getItemBlockStateData(ItemStack items) {
		return items.getTagElement(BlockItem.BLOCK_STATE_TAG);
	}

	private CompoundTag getItemBlockEntityData(ItemStack items) {
		return items.getTagElement(BlockItem.BLOCK_ENTITY_TAG);
	}

	@SafeVarargs
	private TagKey<Block> filterBlockTag(BlockState blockstate, TagKey<Block>... keys) {
		for (int i = 0;i < keys.length;i++ ) {
			if (blockstate.is(keys[i]))
				return keys[i];
		}
		return null;
	}
	
	private String convertNameFromBlockTagKey(TagKey<Block> key) {
		if (key == null)                                { return null; } else
		if (key.equals(BlockTags.NEEDS_DIAMOND_TOOL))    { return "diamond"; } else
		if (key.equals(BlockTags.NEEDS_IRON_TOOL))       { return "iron"; } else
		if (key.equals(BlockTags.NEEDS_STONE_TOOL))      { return "stone"; } else
		if (key.equals(BlockTags.MINEABLE_WITH_PICKAXE)) { return "pickaxe"; } else
		if (key.equals(BlockTags.MINEABLE_WITH_AXE))     { return "axe"; } else
		if (key.equals(BlockTags.MINEABLE_WITH_SHOVEL))  { return "shovel"; } else
		if (key.equals(BlockTags.MINEABLE_WITH_HOE))     { return "hoe"; } else
		if (key.equals(BlockTags.SWORD_EFFICIENT))       { return "sword"; } else {
			return null;
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
