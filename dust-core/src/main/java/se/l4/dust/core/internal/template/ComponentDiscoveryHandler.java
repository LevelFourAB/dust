package se.l4.dust.core.internal.template;

import java.util.Collection;

import javax.print.attribute.standard.Copies;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import se.l4.dust.api.NamespaceManager.Namespace;
import se.l4.dust.api.TemplateManager;
import se.l4.dust.api.TemplateManager.TemplateNamespace;
import se.l4.dust.api.annotation.Component;
import se.l4.dust.api.discovery.DiscoveryEncounter;
import se.l4.dust.api.discovery.DiscoveryHandler;

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
	
	private final TemplateManager templates;

	@Inject
	public ComponentDiscoveryHandler(TemplateManager templates)
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
	}

}
