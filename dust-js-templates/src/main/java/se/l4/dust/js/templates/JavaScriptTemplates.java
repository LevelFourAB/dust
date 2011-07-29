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
	}
	
	Builder addAsset(String namespace, String pathToFile);
}
