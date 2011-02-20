package se.l4.dust.api;

import java.util.List;

import org.jdom.Namespace;

import se.l4.dust.api.annotation.Component;
import se.l4.dust.api.template.PropertySource;

/**
 * Manager of template related information such as registered filters and
 * components.
 * 
 * @author Andreas Holstenson
 *
 */
public interface TemplateManager
{
	/**
	 * Add a component to the manager, the component will be made available
	 * for usage within templates. The name of the component is resolved via
	 * a {@link Component} annotation placed on the actual class.
	 * 
	 * @param ns
	 * 		namespace of component
	 * @param component
	 * 		the class of the component
	 */
	void addComponent(Namespace ns, Class<?> component);
	
	/**
	 * Add a component to the manager using one or more custom names.
	 * 
	 * @param ns
	 * 		namespace of component
	 * @param component
	 * 		the class of the component
	 * @param names
	 * 		names of the component
	 */
	void addComponent(Namespace ns, Class<?> component, String... names);
	
	/**
	 * Check if the given namespace has any components associated with it.
	 * 
	 * @param ns
	 * @return
	 */
	boolean isComponentNamespace(Namespace ns);
	
	/**
	 * Add a filter that should be run for any templates.
	 * 
	 * @param filter
	 */
	void addFilter(TemplateFilter filter);
	
	/**
	 * Get all active filters.
	 * 
	 * @return
	 */
	List<TemplateFilter> getFilters();

	/**
	 * Retrieve a component in the given namespace with the specified name.
	 * If the component is not found this will throw {@link ComponentException}.
	 * 
	 * @param ns
	 * 		namespace of component
	 * @param name
	 * 		name of component
	 * @return
	 */
	Class<?> getComponent(Namespace ns, String name);
	
	/**
	 * Add a property source that can be used within {@literal ${}} expansions
	 * in templates.
	 * 
	 * @param prefix
	 * 		prefix of the source (used in expansions, examples: var, asset)
	 * @param source
	 * 		the source to register
	 */
	void addPropertySource(String prefix, PropertySource source);
	
	/**
	 * Get the property source associated with given prefix.
	 * 
	 * @param prefix
	 * 		the prefix to lookup
	 * @return
	 */
	PropertySource getPropertySource(String prefix);

	/**
	 * Add a property source that can be used within {@literal ${}} expansions
	 * in templates.
	 * 
	 * @param prefix
	 * 		prefix of the soruce (used in expansions, examples: var, asset)
	 * @param type
	 * 		the class of the source
	 */
	void addPropertySource(String prefix, Class<? extends PropertySource> type);
}
