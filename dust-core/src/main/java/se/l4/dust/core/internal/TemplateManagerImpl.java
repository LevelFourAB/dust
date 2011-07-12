package se.l4.dust.core.internal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.google.common.base.Function;
import com.google.common.collect.MapMaker;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

import se.l4.dust.api.ComponentException;
import se.l4.dust.api.TemplateManager;
import se.l4.dust.api.annotation.Component;
import se.l4.dust.api.template.spi.PropertySource;

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
	public TemplateManagerImpl(Injector injector)
	{
		this.injector = injector;
		
		propertySources = new ConcurrentHashMap<String, Object>();
		namespaces = new MapMaker()
			.makeComputingMap(new Function<String, NamespacedTemplateImpl>()
			{
				public NamespacedTemplateImpl apply(String in)
				{
					return new NamespacedTemplateImpl(in);
				}
			});
	}
	
	public NamespacedTemplate getNamespace(String nsUri)
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
		implements NamespacedTemplate
	{
		private final String namespace;
		private final Map<String, Class<?>> components;
		
		public NamespacedTemplateImpl(String namespace)
		{
			this.namespace = namespace;
			components = new ConcurrentHashMap<String, Class<?>>();
		}
		
		public NamespacedTemplate addComponent(Class<?> component)
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
				String name = component.getSimpleName().toLowerCase();
				addComponent(component, name);
			}
			
			return this;
		}

		public NamespacedTemplate addComponent(Class<?> component, String... names)
		{
			for(String name : names)
			{
				components.put(name, component);
			}
			
			return this;
		}

		public Class<?> getComponent(String name)
		{
			Class<?> o = components.get(name);
			if(o == null)
			{
				throw new ComponentException("Unknown component " + name + " in " + namespace);
			}
			return o;
		}

		public boolean hasComponent(String name)
		{
			return components.containsKey(name);
		}
	}
}
