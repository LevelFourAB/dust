package se.l4.dust.js.templates;

/**
 * JavaScript template support.
 * 
 * @author Andreas Holstenson
 *
 */
public interface JavaScriptTemplates
{
	interface Builder
	{
		/**
		 * Add a component to the built asset.
		 * 
		 * @param component
		 * @return
		 */
		Builder addComponent(Class<?> component);
		
		/**
		 * Add a named component to the built asset.
		 * 
		 * @param name
		 * @param component
		 * @return
		 */
		Builder addComponent(String name, Class<?> component);

		/**
		 * Get the default name of an exposed component.
		 * 
		 * @param component
		 * @return
		 */
		String getName(Class<?> component);
	}
	
	Builder addAsset(String namespace, String pathToFile);
}
