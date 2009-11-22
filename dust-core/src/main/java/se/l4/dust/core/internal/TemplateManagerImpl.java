package se.l4.dust.core.internal;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

import org.jdom.Namespace;

import se.l4.dust.api.ComponentException;
import se.l4.dust.api.TemplateFilter;
import se.l4.dust.api.TemplateManager;
import se.l4.dust.api.annotation.Component;
import se.l4.dust.api.template.PropertySource;
import se.l4.dust.core.internal.template.dom.TemplateComponent;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

@Singleton
public class TemplateManagerImpl
	implements TemplateManager
{
	private final ConcurrentMap<Key, Class<?>> components;
	private final Set<Namespace> namespaces;
	private final Injector injector;
	private final List<TemplateFilter> filters;
	private final ConcurrentMap<String, PropertySource> propertySources;
	
	@Inject
	public TemplateManagerImpl(Injector injector)
	{
		this.injector = injector;
		
		components = new ConcurrentHashMap<Key,  Class<?>>();
		namespaces = new CopyOnWriteArraySet<Namespace>();
		
		filters = new CopyOnWriteArrayList<TemplateFilter>();
		propertySources = new ConcurrentHashMap<String, PropertySource>();
	}

	public void addComponent(Class<? extends TemplateComponent> type)
	{
		// Instances of TemplateComponent need special handling to get their name
		TemplateComponent c = (TemplateComponent) injector.getInstance(type);
		Key k = new Key(c.getNamespace(), c.getName());
		components.put(k, type);
		namespaces.add(c.getNamespace());
	}
	
	public void addComponent(Namespace ns, Class<?> component)
	{
		Component annotation = component.getAnnotation(Component.class);
		if(annotation != null)
		{
			addComponent(ns, component, annotation.value());
		}
		else
		{
			String name = component.getSimpleName().toLowerCase();
			addComponent(ns, component, name);
		}
	}

	public void addComponent(Namespace ns, Class<?> component, String... names)
	{
		for(String name : names)
		{
			Key k = new Key(ns, name);
			components.put(k, component);
		}
		namespaces.add(ns);
	}

	public Class<?> getComponent(Namespace ns, String name)
	{
		Class<?> o = components.get(new Key(ns, name));
		if(o == null)
		{
			throw new ComponentException("Unknown component " + name + " in " + ns);
		}
		
		return o;
	}

	public void addFilter(TemplateFilter filter)
	{
		filters.add(filter);
	}
	
	public List<TemplateFilter> getFilters()
	{
		return filters;
	}

	public boolean isComponentNamespace(Namespace ns)
	{
		return namespaces.contains(ns);
	}
	
	public void addPropertySource(String prefix, PropertySource source)
	{
		propertySources.put(prefix, source);
	}
	
	public PropertySource getPropertySource(String prefix)
	{
		return propertySources.get(prefix);
	}
	
	private static class Key
	{
		private final Namespace ns;
		private final String name;
		
		public Key(Namespace ns, String name)
		{
			this.ns = ns;
			this.name = name;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			result = prime * result + ((ns == null) ? 0 : ns.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if(this == obj)
				return true;
			if(obj == null)
				return false;
			if(getClass() != obj.getClass())
				return false;
			Key other = (Key) obj;
			if(name == null)
			{
				if(other.name != null)
					return false;
			}
			else if(!name.equals(other.name))
				return false;
			if(ns == null)
			{
				if(other.ns != null)
					return false;
			}
			else if(!ns.equals(other.ns))
				return false;
			return true;
		}
		
	}
}
