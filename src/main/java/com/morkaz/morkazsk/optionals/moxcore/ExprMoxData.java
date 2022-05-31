package com.morkaz.morkazsk.optionals.moxcore;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.yggdrasil.Fields;
import com.morkaz.morkazsk.managers.RegisterManager;
import com.morkaz.moxlibrary.other.moxdata.MoxChain;
import com.morkaz.moxlibrary.other.moxdata.MoxData;
import com.morkaz.moxlibrary.other.moxdata.Separator;
import org.bukkit.event.Event;

import java.io.NotSerializableException;
import java.io.StreamCorruptedException;

@Name("Mox Data")
@Description({
		"Creates new instance of Mox Data.",
		"You can create it empty or from text object.",
		"Full tutorial: https://github.com/MorkaZ/MorkazSk/wiki/Mox-Data-tutorial"
})
@Examples({
		"set {_mox.data} to new mox data with main key \"%player%\"",
		"add value player's health with key \"health\" to {_mox.data}",
		"set {_health} to value of \"health\" from {_mox.data}",
		"#IMPORTANT - After server restart, values of MoxData saved in variables will be texts.",
		"#You will have to parse them like it is done in example below:",
		" ",
		"on script load:",
		"\tloop pairs of {mox.data.items}: #Example with mox data that holds item types",
		"\t\tset {_key} to key of pair loop-value",
		"\t\tset {_value} to \"%value of pair loop-value%\" parsed as item #Parsing text to type",
		"\t\tadd pair value {_value} keyed {_key} to {mox.data.items} #It will replace old pair with new one"
})
@RequiredPlugins("MoxCore")
@Since("1.1-beta2")

public class ExprMoxData extends SimpleExpression<MoxData> {

	static {
		RegisterManager.registerType(new ClassInfo<>(MoxData.class, "moxdata")
				.user("moxdata(s)?")
				.name("Mox Data Type")
				.description(
						"Mox Data type. It can hold multiple values indexed by keys in one field called Mox Chain.",
						"Specially created for databases to not create a lot of columns but it can be also used in normal skript features - for example in NBT Tags."
				)
				.usage(
						"new mox data [from %string] [with main key %string%]"
				)
				.examples(
						"set {_mox.data} to new mox data with main key \"%player%\"",
						"add value player's health with key \"health\" to {_mox.data}",
						"set {_health} to value of \"health\" from {_mox.data}",
						"set {_mox.data.text} to \"%{_mox.data}%\"",
						"set {_mox.data.from.text} to {_mox.data.text} parsed as mox data",
						"#Values after this transformation will be saved as string type. You will have to parse/initialize values from strings by yourself. It is good to use mox data for texts and numbers instead of complicated object instances."
				)
				.since("1.1-beta2")
				.defaultExpression(new EventValueExpression<>(MoxData.class))
				.parser(new Parser<MoxData>() {
					@Override
					public MoxData parse(final String moxDataString, final ParseContext context) {
						if (moxDataString.contains(Separator.PAIR.toString()) || moxDataString.contains(Separator.MAIN_KEY.toString())){
							return new MoxData(moxDataString);
						}
						return null;
					}

					@Override
					public boolean canParse(final ParseContext context) {
						return true;
					}

					@Override
					public String toString(final MoxData moxData, final int flags) {
						return moxData.toString();
					}

					@Override
					public String getDebugMessage(final MoxData moxData) {
						return "mox data with main key " + moxData.getMainKey();
					}

					@Override
					public String toVariableNameString(final MoxData moxData) {
						return moxData.toString();
					}

					public String getVariableNamePattern() {
						return ".+";
					}
				})
				.serializer(new Serializer<MoxData>() {
					@Override
					public Fields serialize(final MoxData moxData) throws NotSerializableException {
						final Fields fields = new Fields();
						fields.putObject("content", moxData.toString());
						return fields;
					}

					@Override
					public void deserialize(final MoxData o, final Fields f) throws StreamCorruptedException {
						assert true;
					}

					@Override
					public MoxData deserialize(final Fields fields) throws StreamCorruptedException, NotSerializableException {
						return new MoxData(fields.getObject("content").toString());
					}

					@Override
					public boolean canBeInstantiated() {
						return false;
					}

					@Override
					public boolean mustSyncDeserialization() {
						return false;
					}
				})
		);
		RegisterManager.registerExpression(
				ExprMoxData.class,
				MoxData.class,
				ExpressionType.SIMPLE,
				"(new|empty) mox data",
				"(new|empty) mox data with main key %string%",
				"[new] mox data (of|from) %string%",
				"[new] mox data (of|from) %string% with main key %string%"
		);
	}


	private Expression<String> stringExpr;
	private Expression<String> mainKeyExpr;
	private int pattern = 0;

	public Class<? extends MoxData> getReturnType() {
		return MoxData.class;
	}

	public boolean isSingle() {
		return true;
	}

	public boolean init(Expression<?>[] expressions, int pattern, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
		this.pattern = pattern;
		if (pattern == 1){
			this.mainKeyExpr = (Expression<String>)expressions[0];
		} else if (pattern == 2){
			this.stringExpr = (Expression<String>)expressions[0];
		} else if (pattern == 3){
			this.stringExpr = (Expression<String>)expressions[0];
			this.mainKeyExpr = (Expression<String>)expressions[1];
		}
		return true;
	}

	public String toString(Event event, boolean debug) {
		if (pattern == 0){
			return "empty mox data";
		} else if (pattern == 1){
			return "empty mox data with main key "+mainKeyExpr.toString(event, debug);
		} else if (pattern == 2){
			return "mox data of "+stringExpr.toString(event, debug);
		}
		return "mox data of "+stringExpr.toString(event, debug)+" with main key "+mainKeyExpr.toString(event, debug);
	}

	protected MoxData[] get(Event e) {
		if (pattern == 0){
			MoxData moxData = new MoxData(new MoxChain());
			return new MoxData[]{moxData};
		} else if (pattern == 1) {
			String mainKey = mainKeyExpr.getSingle(e);
			if (mainKey != null){
				MoxData moxData = new MoxData(mainKey, new MoxChain());
				return new MoxData[]{moxData};
			}
		} else if (pattern == 2){
			String moxDataString = stringExpr.getSingle(e);
			if (moxDataString != null){
				MoxData moxData = new MoxData(moxDataString);
				return new MoxData[]{moxData};
			}
		} else if (pattern == 3){
			String moxDataString = stringExpr.getSingle(e);
			String mainKey = mainKeyExpr.getSingle(e);
			if (moxDataString != null && mainKey != null){
				MoxData moxData = new MoxData(moxDataString);
				moxData.setMainKey(mainKey);
				return new MoxData[]{moxData};
			}
		}
		return new MoxData[]{};
	}
}

