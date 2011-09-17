package se.l4.dust.core.internal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.l4.dust.api.ComponentException;
import se.l4.dust.api.NamespaceManager;
import se.l4.dust.api.TemplateManager;
import se.l4.dust.api.annotation.Component;
import se.l4.dust.api.discovery.ClassDiscovery;
import se.l4.dust.api.discovery.DiscoveryFactory;
import se.l4.dust.api.template.spi.PropertySource;

import com.google.common.base.Function;
import com.google.common.collect.MapMaker;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.Stage;

/**
 * Implementation of {@link TemplateManager}. The implementation keeps track
 * of registered component classes, namespaces, property sources and filters.
 * Namespaces are automatically registered based on calls to addComponent. 
 * 
 * @author Andreas Holstenson
 *
 */
@Singleton
public class TemplateManagerImpl
	implements TemplateManager
{
	private final Injector injector;
	private final ConcurrentMap<String, Object> propertySources;
	private final ConcurrentMap<String, NamespacedTemplateImpl> namespaces;
	
	@Inject
	public TemplateManagerImpl(Injector injector, final Stage stage, final DiscoveryFactory discovery, final NamespaceManager nsManager)
	{
		this.injector = injector;
		
		propertySources = new ConcurrentHashMap<String, Object>();
		namespaces = new MapMaker()
			.makeComputingMap(new Function<String, NamespacedTemplateImpl>()
			{
				public NamespacedTemplateImpl apply(String in)
				{
					NamespaceManager.Namespace ns = nsManager.getNamespaceByURI(in);
					ClassDiscovery cd = ns == null ? discovery.empty() : discovery.get(ns.getPackage());
					
					return new NamespacedTemplateImpl(in, cd, stage == Stage.DEVELOPMENT);
				}
			});
	}
	
	public TemplateNamespace getNamespace(String nsUri)
	{
		return namespaces.get(nsUri);
	}

	
	public void addPropertySource(String prefix, PropertySource source)
	{
		propertySources.put(prefix, source);
	}
	
	public void addPropertySource(String prefix, Class<? extends PropertySource> provider)
	{
		propertySources.put(prefix, provider);
	}
	
	public PropertySource getPropertySource(String prefix)
	{
		Object o = propertySources.get(prefix);
		if(o instanceof PropertySource)
		{
			return (PropertySource) o;
		}
		else if(o instanceof Class<?>)
		{
			return (PropertySource) injector.getInstance((Class<?>) o);
		}
		
		return null;
	}
	
	private static class NamespacedTemplateImpl
		implements TemplateNamespace
	{
		private final Logger logger;
		
		private final String namespace;
		private final Map<String, Class<?>> components;
		
		private final ClassDiscovery discovery;
		private final boolean dev;
		
		public NamespacedTemplateImpl(String namespace, ClassDiscovery discovery, boolean dev)
		{
			this.dev = dev;
			logger = LoggerFactory.getLogger(TemplateNamespace.class.getName() + " [" + namespace + "]");
			
			this.namespace = namespace;
			this.discovery = discovery;
			
			components = new ConcurrentHashMap<String, Class<?>>();
			
			for(Class<?> c : discovery.getAnnotatedWith(Component.class))
			{
				addComponent(c);
			}
		}
		
		public TemplateNamespace addComponent(Class<?> component)
		{
			String[] names = null;
			
			Component annotation = component.getAnnotation(Component.class);
			if(annotation != null)
			{
				names = annotation.value();
			}
			
			if(names != null && names.length > 0)
			{
				addComponent(component, names);
			}
			else
			{
				String name = component.getSimpleName();
				addComponent(component, name);
			}
			
			return this;
		}

		public TemplateNamespace addComponent(Class<?> component, String... names)
		{
			for(String name : names)
			{
				components.put(name.toLowerCase(), component);
			}
			
			return this;
		}

		public Class<?> getComponent(String name)
		{
			Class<?> o = components.get(name.toLowerCase());
			if(o == null)
			{
				throw new ComponentException("Unknown component " + name + " in " + namespace);
			}
			
			return o;
		}

		public boolean hasComponent(String name)
		{
			boolean found = components.containsKey(name.toLowerCase());
			if(found)
			{
				return true;
			}
			
			if(dev && discovery != null)
			{
				logger.info("Attempting to discover new component named " + name);
				
				/*
				 * Reindex if we have discovery functions, we might find
				 * the class this time.
				 */
				discovery.index();
				
				for(Class<?> c : discovery.getAnnotatedWith(Component.class))
				{
					addComponent(c);
					
					return components.containsKey(name);
				}
			}
			
			return false;
		}
		
		public String getComponentName(Class<?> component)
		{
			Component annotation = component.getAnnotation(Component.class);
			if(annotation != null)
			{
				String[] names = annotation.value();
				if(names.length > 0)
				{
					return names[0];
				}
			}
			
			return component.getSimpleName().toLowerCase();
		}
	}
}
