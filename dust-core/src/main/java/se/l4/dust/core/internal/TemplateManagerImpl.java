package se.l4.dust.core.internal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.l4.dust.api.ComponentException;
import se.l4.dust.api.NamespaceManager;
import se.l4.dust.api.TemplateManager;
import se.l4.dust.api.annotation.Component;
import se.l4.dust.api.discovery.NamespaceDiscovery;
import se.l4.dust.api.template.mixin.TemplateMixin;
import se.l4.dust.api.template.spi.TemplateFragment;
import se.l4.dust.core.internal.template.dom.ComponentTemplateFragment;

import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
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
	private final LoadingCache<String, NamespacedTemplateImpl> namespaces;
	private final AtomicInteger counter;
	
	@Inject
	public TemplateManagerImpl(Injector injector_, 
			final Stage stage,
			final NamespaceManager nsManager,
			final NamespaceDiscovery discovery)
	{
		this.injector = injector_;
		
		counter = new AtomicInteger();
		
		namespaces = CacheBuilder.newBuilder()
			.build(new CacheLoader<String, NamespacedTemplateImpl>()
			{
				@Override
				public NamespacedTemplateImpl load(String key)
					throws Exception
				{
					NamespaceManager.Namespace ns = nsManager.getNamespaceByURI(key);
					
					return new NamespacedTemplateImpl(injector, key, discovery, stage == Stage.DEVELOPMENT);
				}
			});
	}
	
	public TemplateNamespace getNamespace(String nsUri)
	{
		try
		{
			return namespaces.get(nsUri);
		}
		catch(ExecutionException e)
		{
			throw Throwables.propagate(e.getCause());
		}
	}

	@Override
	public int fetchTemplateId()
	{
		return counter.incrementAndGet();
	}
	
	private static class NamespacedTemplateImpl
		implements TemplateNamespace
	{
		private final Logger logger;
		private final Injector injector;
		
		private final String namespace;
		private final Map<String, TemplateFragment> fragments;
		private final Map<String, TemplateMixin> mixins;
		
		private final boolean dev;
		private final NamespaceDiscovery discovery;
		
		public NamespacedTemplateImpl(Injector injector, String namespace, NamespaceDiscovery discovery, boolean dev)
		{
			this.injector = injector;
			this.dev = dev;
			logger = LoggerFactory.getLogger(TemplateNamespace.class.getName() + " [" + namespace + "]");
			
			this.namespace = namespace;
			this.discovery = discovery;
			
			fragments = new ConcurrentHashMap<>();
			
			mixins = new ConcurrentHashMap<String, TemplateMixin>();
		}
		
		@Override
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

		@Override
		public TemplateNamespace addComponent(Class<?> component, String... names)
		{
			TemplateFragment fragment = new ComponentTemplateFragment(injector, component);
			for(String name : names)
			{
				fragments.put(name.toLowerCase(), fragment);
			}
			
			return this;
		}
		
		@Override
		public TemplateNamespace addFragment(String name, TemplateFragment fragment)
		{
			fragments.put(name, fragment);
			
			return this;
		}

		@Override
		public TemplateFragment getFragment(String name)
		{
			TemplateFragment o = fragments.get(name.toLowerCase());
			if(o == null)
			{
				throw new ComponentException("Unknown component " + name + " in " + namespace);
			}
			
			return o;
		}

		@Override
		public boolean hasFragment(String name)
		{
			boolean found = fragments.containsKey(name.toLowerCase());
			if(found)
			{
				return true;
			}
			
			if(dev)
			{
				logger.info("Attempting to discover new template fragment named " + name + " in " + namespace);
				
				/*
				 * Reindex if we have discovery functions, we might find
				 * the class this time.
				 */
				discovery.performDiscovery();
				
				return fragments.containsKey(name);
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
		
		@Override
		public TemplateNamespace addMixin(String attribute, TemplateMixin mixin)
		{
			mixins.put(attribute, mixin);
			return this;
		}
		
		@Override
		public TemplateMixin getMixin(String attribute)
		{
			return mixins.get(attribute);
		}
		
		@Override
		public boolean hasMixin(String attribute)
		{
			return mixins.containsKey(attribute);
		}
	}
}
