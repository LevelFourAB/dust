package se.l4.dust.api;

import java.util.List;

import org.jdom.Namespace;

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
	void addComponent(Namespace ns, Class<?> component);
	
	void addComponent(Namespace ns, Class<?> component, String... names);
	
	boolean isComponentNamespace(Namespace ns);
	
	void addFilter(TemplateFilter filter);
	
	List<TemplateFilter> getFilters();

	Class<?> getComponent(Namespace ns, String name);
	
	/**
	 * Add a property source that can be used within {@literal ${}} expansions
	 * in templates.
	 * 
	 * @param prefix
	 * @param source
	 */
	void addPropertySource(String prefix, PropertySource source);
	
	PropertySource getPropertySource(String prefix);
}
