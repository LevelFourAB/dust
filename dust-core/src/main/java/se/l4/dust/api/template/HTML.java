package se.l4.dust.api.template;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

/**
 * Utilities for HTML tags.
 *
 * @author Andreas Holstenson
 *
 */
public class HTML
{
	private static final Set<String> singleTags;
	private static final Set<String> inlineTags;

	static
	{
		singleTags = ImmutableSet.<String>builder()
			.add("br")
			.add("hr")
			.add("img")
			.add("link")
			.add("meta")
			.add("input")
			.add("area")
			.add("base")
			.add("col")
			.add("command")
			.add("embed")
			.add("keygen")
			.add("param")
			.add("source")
			.add("track")
			.add("wbr")
			.build();

		inlineTags = ImmutableSet.<String>builder()
			.add("b").add("big").add("i").add("small").add("tt")
			.add("abbr").add("acronym").add("cite").add("code").add("dfn")
			.add("em").add("kbd").add("strong").add("samp").add("var")
			.add("a").add("bdo").add("br").add("img").add("map").add("object")
			.add("q").add("script").add("span").add("sub").add("sup")
			.add("button").add("input").add("label").add("select").add("textarea")
			.build();
	}

	public static boolean isSingle(String tagName)
	{
		return singleTags.contains(tagName.toLowerCase());
	}

	public static boolean isInline(String tagName)
	{
		return inlineTags.contains(tagName.toLowerCase());
	}
}
