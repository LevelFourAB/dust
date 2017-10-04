package se.l4.dust.servlet.internal.routing;

import java.util.regex.Pattern;

/**
 * Matcher that will match based on regular expressions.
 *
 * @author Andreas Holstenson
 *
 */
public class RegexMatcher
	implements Matcher
{
	private final Pattern pattern;

	public RegexMatcher(String regex)
	{
		pattern = Pattern.compile(regex);
	}

	public boolean matches(String path)
	{
		return pattern.matcher(path).matches();
	}

}
