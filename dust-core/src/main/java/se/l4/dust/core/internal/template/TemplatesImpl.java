package se.l4.dust.core.internal.template;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.Stage;

import se.l4.dust.api.discovery.NamespaceDiscovery;
import se.l4.dust.api.template.Component;
import se.l4.dust.api.template.ComponentException;
import se.l4.dust.api.template.Templates;
import se.l4.dust.api.template.fragment.TemplateFragment;
import se.l4.dust.api.template.mixin.TemplateMixin;
import se.l4.dust.core.internal.template.components.ComponentTemplateFragment;

/**
 * Implementation of {@link Templates}. The implementation keeps track
 * of registered component classes, namespaces, property sources and filters.
 * Namespaces are automatically registered based on calls to addComponent.
 *
 * @author Andreas Holstenson
 *
 */
@Singleton
public class TemplatesImpl
	implements Templates
{
	private final Injector injector;
	private final LoadingCache<String, TemplateNamespaceImpl> namespaces;
	private final AtomicInteger counter;

	@Inject
	public TemplatesImpl(Injector injector_,
			final Stage stage,
			final NamespaceDiscovery discovery)
	{
		this.injector = injector_;

		counter = new AtomicInteger();

		namespaces = CacheBuilder.newBuilder()
			.build(new CacheLoader<String, TemplateNamespaceImpl>()
			{
				@Override
				public TemplateNamespaceImpl load(String key)
					throws Exception
				{
					return new TemplateNamespaceImpl(TemplatesImpl.this, injector, key, discovery, stage == Stage.DEVELOPMENT);
				}
			});
	}

	@Override
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

	private static class TemplateNamespaceImpl
		implements TemplateNamespace
	{
		private final TemplatesImpl templates;

		private final Logger logger;
		private final Injector injector;

		private final String namespace;
		private final Map<String, TemplateFragment> fragments;
		private final Map<String, TemplateMixin> mixins;

		private final boolean dev;
		private final NamespaceDiscovery discovery;

		public TemplateNamespaceImpl(TemplatesImpl templates, Injector injector, String namespace, NamespaceDiscovery discovery, boolean dev)
		{
			this.templates = templates;
			this.injector = injector;
			this.dev = dev;
			logger = LoggerFactory.getLogger(TemplateNamespace.class.getName() + " [" + namespace + "]");

			this.namespace = namespace;
			this.discovery = discovery;

			fragments = new ConcurrentHashMap<>();

			mixins = new ConcurrentHashMap<>();
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

			if(names == null || names.length == 0)
			{
				names = new String[] { component.getSimpleName() };
			}

			addComponent(component, names);

			return this;
		}

		@Override
		public TemplateNamespace addComponent(Class<?> component, String... names)
		{
			TemplateFragment fragment;
			if(TemplateFragment.class.isAssignableFrom(component))
			{
				fragment = (TemplateFragment) injector.getInstance(component);
			}
			else
			{
				fragment = new ComponentTemplateFragment(injector, component);
			}

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

		@Override
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
		public TemplateNamespace addComponentOverride(String namespace, Class<?> originalComponent, Class<?> newComponent)
		{
			Component annotation = originalComponent.getAnnotation(Component.class);
			if(annotation == null)
			{
				throw new ComponentException("Unable to override " +
					originalComponent.getSimpleName() + " in " +
					namespace + "; It is not annotated with @" +
					Component.class.getSimpleName());
			}

			String[] names = annotation.value();
			if(names == null || names.length == 0)
			{
				names = new String[] { originalComponent.getSimpleName() };
			}

			// TODO: Verify that no extra methods exist

			if(! originalComponent.isAssignableFrom(newComponent))
			{
				throw new ComponentException("Unable to override " +
					originalComponent.getSimpleName() + " in " +
					namespace + "; New component does not overide old one");
			}

			TemplateNamespaceImpl other = (TemplateNamespaceImpl) templates.getNamespace(namespace);
			if(! other.fragments.containsKey(names[0]))
			{
				throw new ComponentException("Unable to override " +
					originalComponent.getSimpleName() + " in " +
					namespace + "; Old component does not seem to be registered");
			}

			other.addComponent(newComponent, names);

			return this;
		}

		@Override
		public TemplateNamespace addMixin(String attribute, TemplateMixin mixin)
		{
			mixins.put(attribute, mixin);
			return this;
		}

		@Override
		public TemplateNamespace addMixin(TemplateMixin mixin)
		{
			mixins.put("", mixin);
			return this;
		}

		@Override
		public TemplateMixin getMixin(String attribute)
		{
			return mixins.get(attribute);
		}

		@Override
		public TemplateMixin getDefaultMixin()
		{
			return mixins.get("");
		}

		@Override
		public boolean hasMixin(String attribute)
		{
			return mixins.containsKey(attribute);
		}
	}
}
