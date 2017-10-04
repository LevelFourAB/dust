package se.l4.dust.servlet.internal.routing;

public class AnyMatcher
	implements Matcher
{
	public static final Matcher INSTANCE = new AnyMatcher();

	private AnyMatcher()
	{
	}

	@Override
	public boolean matches(String path)
	{
		return true;
	}

}
