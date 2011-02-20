package se.l4.dust.core.internal.template.dom;

import org.jdom.Namespace;

import se.l4.dust.api.TemplateManager;
import se.l4.dust.api.template.TemplateCache;
import se.l4.dust.dom.Element;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

/**
 * Internal service that actually builds an instance of a component for 
 * usage in a parsed template.
 * 
 * @author Andreas Holstenson
 *
 */
@Singleton
public class TemplateComponentBuilder
{
	private final TemplateManager manager;
	private final TemplateCache cache;
	private final Injector injector;

	@Inject
	public TemplateComponentBuilder(
			TemplateManager manager,
			TemplateCache cache,
			Injector injector)
	{
		this.manager = manager;
		this.cache = cache;
		this.injector = injector;
	}
	
	public Element build(Namespace ns, String name)
	{
		Class<?> o = manager.getComponent(ns, name);
			
		if(TemplateComponent.class.isAssignableFrom(o))
		{
			return (TemplateComponent) injector.getInstance(o);
		}
		else
		{
			return new ClassTemplateComponent(ns, name, injector, cache, (Class) o);
		}
	}
}
