package se.l4.dust.servlet.internal.routing;

/**
 * Matcher for URLs used in filtering and servlet handling.
 * 
 * @author Andreas Holstenson
 *
 */
public interface Matcher
{
	boolean matches(String path);
}
