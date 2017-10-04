package se.l4.dust.servlet.internal.routing;

/**
 * Matcher that matches against the syntax used in web.xml.
 *
 * @author Andreas Holstenson
 *
 */
public class NormalMatcher
	implements Matcher
{
	enum Match
	{
		EXACT,
		BEGIN,
		END
	}

	private final Match match;
	private final String path;

	public NormalMatcher(String path)
	{
		if(path.startsWith("*"))
		{
			this.path = path.substring(1);
			this.match = Match.BEGIN;
		}
		else if(path.endsWith("*"))
		{
			this.path = path.substring(0, path.length()-1);
			this.match = Match.END;
		}
		else
		{
			this.path = path;
			this.match = Match.EXACT;
		}
	}

	public boolean matches(String path)
	{
		switch(match)
		{
			case BEGIN:
				return path.endsWith(this.path);
			case END:
				return path.startsWith(this.path);
			default:
				return path.equals(this.path);
		}
	}
}
