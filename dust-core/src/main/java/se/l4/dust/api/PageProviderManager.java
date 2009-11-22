package se.l4.dust.api;

/**
 * Manager of {@link PageProvider}s.
 * 
 * @author Andreas Holstenson
 *
 */
public interface PageProviderManager
{
	void add(PageProvider factory);
	
	void remove(PageProvider factory);
}
