package jp.ne.clane.espAnvil;

import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jp.ne.clane.commons.EffectUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CompassItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.LodestoneTracker;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

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

	public EspAnvilMain(IEventBus modEventBus, ModContainer modContainer) {
		instance = this;
		IEventBus bus = modEventBus;
		bus.addListener(this::setup);

		NeoForge.EVENT_BUS.register(this);
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
		Item item = items.getItem();
		List<Component> tooltip = ev.getToolTip();
		if (items.isCombineRepairable() || (items.getComponents().get(DataComponents.STORED_ENCHANTMENTS) != null)) {
			if (EspAnvilConfig.isShowAnvilCount) {
				tooltip.add(Component.translatable("clane.mod.espAnvil.anvilUseCount", Math.round(Math.log(items.getOrDefault(DataComponents.REPAIR_COST, Integer.valueOf(0)) + 1) / Math.log(2))));
			}
			if (EspAnvilConfig.isShowItemDurability && !ev.getFlags().isAdvanced() && items.isCombineRepairable()) {
				tooltip.add(Component.translatable("clane.mod.espAnvil.itemDurability", items.getMaxDamage() - items.getDamageValue(), items.getMaxDamage()));
			}
		} else if (items.get(DataComponents.FOOD) != null) {
			FoodProperties foodProp = items.get(DataComponents.FOOD);
			FoodData currentFoodData = mcInst.player.getFoodData();
			int afterNutrationLevel = Math.min(20, currentFoodData.getFoodLevel() + foodProp.nutrition());
			if (EspAnvilConfig.isShowNutrition) {
				MutableComponent addCompNut = Component.translatable("clane.mod.espAnvil.nutrition");
				addCompNut.append(Component.translatable("clane.mod.espAnvil.hungerValueAdd",foodProp.nutrition()));
				if (EspAnvilConfig.isShowNutritionBeforeAfter) {
					addCompNut.append(Component.translatable("clane.mod.espAnvil.hungerValue.BeforeAfter", currentFoodData.getFoodLevel(), afterNutrationLevel));
				}
				tooltip.add(addCompNut);
			}
			if (EspAnvilConfig.isShowSaturation) {
				Float addSaturationValue = (float)foodProp.saturation();
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
				float eatSeconds = item.getUseDuration(items, null); //ハチミツ入りの瓶がitemクラスの上書きで実装されている
				if (eatSeconds < 32) {
					optionValue.append(Component.translatable("clane.mod.espAnvil.food.fastfood").getString()).append(optionSeparater);
				} else if (eatSeconds > 32) {
					optionValue.append(Component.translatable("clane.mod.espAnvil.food.slowfood").getString()).append(optionSeparater);
				}
				if (foodProp.canAlwaysEat()) {
					optionValue.append(Component.translatable("clane.mod.espAnvil.food.alwayseat").getString()).append(optionSeparater);
				}
				if (items.has(DataComponents.SUSPICIOUS_STEW_EFFECTS) && !ev.getFlags().isCreative()) {
		            for (SuspiciousStewEffects.Entry effectEntry : items.getOrDefault(DataComponents.SUSPICIOUS_STEW_EFFECTS, SuspiciousStewEffects.EMPTY).effects()) {
		            	optionValue.append(EffectUtils.getEffectDescribeText(effectEntry, optionSeparater));
		            }
				}
				if (items.get(DataComponents.CONSUMABLE) != null) {
					for (ConsumeEffect consumeEffect : items.get(DataComponents.CONSUMABLE).onConsumeEffects() ) {
						if (consumeEffect.getType() == ConsumeEffect.Type.APPLY_EFFECTS) {
							ApplyStatusEffectsConsumeEffect applyStatusEffect = ((ApplyStatusEffectsConsumeEffect)consumeEffect);
							for (MobEffectInstance mobEffect : applyStatusEffect.effects()) {
								optionValue.append(EffectUtils.getEffectDescribeText(mobEffect, applyStatusEffect.probability(), optionSeparater));
							}
						} else if (consumeEffect.getType() == ConsumeEffect.Type.REMOVE_EFFECTS) {
							//RemoveStatusEffectsConsumeEffect.effectsの不可視が解消できないのでスキップ。mixinするまでもなかろ。てかそもそもなんで不可視なんだコレ
							//RemoveStatusEffectsConsumeEffect removeStatusEffect = ((RemoveStatusEffectsConsumeEffect)consumeEffect);
							//for (MobEffect mobEffect : removeStatusEffect.effects) {
							//	
							//}
						} else if (consumeEffect.getType() == ConsumeEffect.Type.CLEAR_ALL_EFFECTS) {
							optionValue.append(Component.translatable("clane.mod.espAnvil.food.clearEffect").getString()).append(optionSeparater);
						} else if (consumeEffect.getType() == ConsumeEffect.Type.TELEPORT_RANDOMLY) {
							optionValue.append(Component.translatable("clane.mod.espAnvil.food.randomTeleport").getString()).append(optionSeparater);
						}
					}
				}
				if (optionValue.length() > 0) {
					tooltip.add(Component.literal(optionValue.delete(optionValue.length() - optionSeparater.length(), optionValue.length()).toString()));
				}
			}
		} else if (item instanceof BlockItem) {
			Block block = ((BlockItem)item).getBlock();;
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
				//適宜追加
			}
		} else if (item instanceof CompassItem) {
			LodestoneTracker tracker = items.get(DataComponents.LODESTONE_TRACKER);
			if (tracker != null) {
				GlobalPos gpos = tracker.target().get();
				tooltip.add(Component.translatable("clane.mod.espAnvil.compass.target", Component.translatable(gpos.dimension().identifier().toLanguageKey()).getString(), gpos.pos().getX(), gpos.pos().getY(), gpos.pos().getZ()));
			}
		}
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
			config.loadConfig(config.getConfigFile());
		} catch (IllegalAccessException | IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		try {
			config.saveConfig(config.getConfigFile());
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
