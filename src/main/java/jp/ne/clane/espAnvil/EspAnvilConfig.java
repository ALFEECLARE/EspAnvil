package jp.ne.clane.espAnvil;

import jp.ne.clane.commons.ConfigBase;

public class EspAnvilConfig extends ConfigBase {
	  public static boolean isShowAnvilCount = true;
	  public static boolean isShowItemDurability = true;
	  public static boolean isShowNutrition = true;
	  public static boolean isShowNutritionBeforeAfter = true;
	  public static boolean isShowSaturation = true;
	  public static boolean isShowSaturationBeforeAfter = true;
	  public static boolean isShowFoodExtraInfo = true;
	  public static boolean isShowBlockDestroyTime = true;
	  public static boolean isShowExplosionResistance = true;
	  public static boolean isShowLightLevel = true;
	  
	  public EspAnvilConfig() {
		  super(EspAnvilConfig.class);
	  }
}
