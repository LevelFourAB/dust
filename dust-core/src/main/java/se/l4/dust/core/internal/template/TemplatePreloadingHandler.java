package se.l4.dust.core.internal.template;

import java.io.IOException;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.l4.dust.api.Context;
import se.l4.dust.api.DefaultContext;
import se.l4.dust.api.NamespaceManager.Namespace;
import se.l4.dust.api.TemplateException;
import se.l4.dust.api.TemplateManager;
import se.l4.dust.api.TemplateManager.TemplateNamespace;
import se.l4.dust.api.annotation.Component;
import se.l4.dust.api.annotation.Template;
import se.l4.dust.api.discovery.DiscoveryEncounter;
import se.l4.dust.api.discovery.DiscoveryHandler;
import se.l4.dust.api.template.TemplateCache;

import com.google.inject.Inject;

public class TemplatePreloadingHandler
	implements DiscoveryHandler
{
	private static final Logger logger = LoggerFactory.getLogger(DiscoveryHandler.class);
	
	private final TemplateManager templates;
	private final TemplateCache cache;

	@Inject
	public TemplatePreloadingHandler(TemplateManager templates, TemplateCache cache)
	{
		this.templates = templates;
		this.cache = cache;
	}

	@Override
	public void handle(Namespace ns, DiscoveryEncounter encounter)
	{
		TemplateNamespace ts = templates.getNamespace(ns.getUri());
		
		Context context = new DefaultContext();
		
		try
		{
			Collection<Class<?>> components = encounter.getAnnotatedWith(Component.class);
			for(Class<?> c : components)
			{
				cache.getTemplate(context, c, c.getAnnotation(Template.class));
			}
			
			Collection<Class<?>> templates = encounter.getAnnotatedWith(Template.class);
			for(Class<?> c : templates)
			{
				cache.getTemplate(context, c, c.getAnnotation(Template.class));
			}
			
			logger.debug("{}: Loaded {} templates", ns.getUri(), components.size() + templates.size());
		}
		catch(IOException e)
		{
			throw new TemplateException("IO problem; " + e.getMessage(), e);
		}
	}
}
