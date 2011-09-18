package se.l4.dust.core.internal;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import se.l4.crayon.Contributions;

/**
 * Static tracker for contributions. Needed to integrate with JRebel.
 * 
 * @author Andreas Holstenson
 *
 */
public class InternalContributions
{
	private static final List<Contributions> tracked;
	
	static
	{
		tracked = new CopyOnWriteArrayList<Contributions>();
	}
	
	private InternalContributions()
	{
	}
	
	/**
	 * Add a new {@link Contributions} to track.
	 * 
	 * @param contributions
	 */
	public static void add(Contributions contributions)
	{
		tracked.add(contributions);
	}
	
	/**
	 * Rerun all contributions.
	 * 
	 */
	public static void rerun()
	{
		for(Contributions c : tracked)
		{
			c.run();
		}
	}
}
