package jp.ne.clane.espAnvil;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
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

	private static EspAnvilMain instance;

	//private EspAnvilConfig config;

	private static void log(String message) {
		log.info("[{}] {}", log.getName(), message);
	}

	/**
	 * Reload modules
	 */
	public EspAnvilMain modules() {
		try {
			Minecraft mc = Minecraft.getInstance();
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
		ItemStack items = ev.getItemStack();
		if (!items.isRepairable() && EnchantedBookItem.getEnchantments(items).isEmpty()) {
			return;
		}
		List<Component> tooltip = ev.getToolTip();
		tooltip.add(Component.translatable("clane.mod.espAnvil.anvilUseCount", Math.round(Math.log(items.getBaseRepairCost() + 1) / Math.log(2))));
		if (!ev.getFlags().isAdvanced() && items.isRepairable()) {
			tooltip.add(Component.translatable("clane.mod.espAnvil.itemDurability", items.getMaxDamage() - items.getDamageValue(), items.getMaxDamage()));
		}
	}

	private void setup(final FMLCommonSetupEvent event) {
		log("espAnvil Init");
	}
}
