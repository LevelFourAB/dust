package se.l4.dust.core.internal.template;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.l4.dust.api.Namespace;
import se.l4.dust.api.discovery.DiscoveryEncounter;
import se.l4.dust.api.discovery.DiscoveryHandler;
import se.l4.dust.api.template.Component;
import se.l4.dust.api.template.ComponentOverride;
import se.l4.dust.api.template.Templates;
import se.l4.dust.api.template.Templates.TemplateNamespace;

import com.google.inject.Inject;

/**
 * Handler that registers components annotated with {@link Component}.
 * 
 * @author Andreas Holstenson
 *
 */
public class ComponentDiscoveryHandler
	implements DiscoveryHandler
{
	private static final Logger logger = LoggerFactory.getLogger(DiscoveryHandler.class);
	
	private final Templates templates;

	@Inject
	public ComponentDiscoveryHandler(Templates templates)
	{
		this.templates = templates;
	}

	@Override
	public void handle(Namespace ns, DiscoveryEncounter encounter)
	{
		TemplateNamespace ts = templates.getNamespace(ns.getUri());
		
		Collection<Class<?>> components = encounter.getAnnotatedWith(Component.class);
		for(Class<?> c : components)
		{
			ts.addComponent(c);
		}
		
		logger.debug("{}: Found {} components", ns.getUri(), components.size());
		
		components = encounter.getAnnotatedWith(ComponentOverride.class);
		for(Class<?> c : components)
		{
			ComponentOverride co = c.getAnnotation(ComponentOverride.class);
			ts.addComponentOverride(co.namespace(), co.component(), c);
		}
		
		if(! components.isEmpty())
		{
			logger.debug("{}: {} components were overridden", ns.getUri(), components.size());
		}
	}

}
