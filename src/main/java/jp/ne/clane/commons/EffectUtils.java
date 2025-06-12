package jp.ne.clane.commons;

import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.component.SuspiciousStewEffects;

public class EffectUtils {

	public static StringBuilder getEffectDescribeText(SuspiciousStewEffects.Entry effectEntry, String optionSeparater) {
		return getEffectDescribeTextCore(effectEntry.createEffectInstance()).append(")").append(optionSeparater);
	}

	public static StringBuilder getEffectDescribeText(MobEffectInstance effectIns, float effectProbability, String optionSeparater) {
		StringBuilder answer = getEffectDescribeTextCore(effectIns);
		if (effectProbability != 1.0f) {
			answer.append(" - ").append(String.valueOf(Math.round(effectProbability * 100))).append("%");
		}
		return answer.append(")").append(optionSeparater);
	}

	//効果時間部分のかっこを閉じないので、呼び側で閉じて改行する。
	private static StringBuilder getEffectDescribeTextCore(MobEffectInstance effectIns) {
		StringBuilder answer = new StringBuilder();
		int duratationSecond = effectIns.getDuration() / 20;
		answer.append(effectIns.getEffect().value().getDisplayName().getString()).append(effectIns.getAmplifier() > 0 ? Component.translatable("enchantment.level." + (effectIns.getAmplifier() + 1)).getString() : "")
			.append("(").append(String.format("%01d:%02d",(int)duratationSecond / 60,(int)duratationSecond % 60));
		return answer;
	}
}
