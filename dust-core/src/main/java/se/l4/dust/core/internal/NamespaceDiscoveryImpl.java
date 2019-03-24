package se.l4.dust.core.internal;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import se.l4.commons.types.TypeFinder;
import se.l4.commons.types.TypeFinderBuilder;
import se.l4.dust.api.Namespace;
import se.l4.dust.api.Namespaces;
import se.l4.dust.api.discovery.DiscoveryEncounter;
import se.l4.dust.api.discovery.DiscoveryHandler;
import se.l4.dust.api.discovery.NamespaceDiscovery;

@Singleton
public class NamespaceDiscoveryImpl
	implements NamespaceDiscovery
{
	private final Namespaces namespaces;
	private final List<DiscoveryHandler> handlers;

	@Inject
	public NamespaceDiscoveryImpl(Namespaces namespaces)
	{
		this.namespaces = namespaces;
		handlers = Lists.newArrayList();
	}

	@Override
	public void performDiscovery()
	{
		TypeFinderBuilder builder = TypeFinder.builder();
		for(Namespace ns : namespaces.list())
		{
			if(ns.getPackage() == null || ns.getPackage().isEmpty()) continue;

			builder.addPackage(ns.getPackage());
		}

		TypeFinder finder = builder.build();
		DiscoveryEncounterImpl encounter = new DiscoveryEncounterImpl(finder);
		for(DiscoveryHandler handler : handlers)
		{
			for(Namespace ns : namespaces.list())
			{
				if(ns.getPackage() != null)
				{
					encounter.setPackage(ns.getPackage());
					handler.handle(ns, encounter);
				}
			}
		}
	}

	@Override
	public void addHandler(DiscoveryHandler handler)
	{
		handlers.add(handler);
	}

	private static class DiscoveryEncounterImpl
		implements DiscoveryEncounter
	{
		private final TypeFinder finder;
		private Predicate<Class<?>> filter;

		public DiscoveryEncounterImpl(TypeFinder finder)
		{
			this.finder = finder;
		}

		public void setPackage(String pkg)
		{
			final String pkgFilter = pkg + ".";
			filter = new Predicate<Class<?>>()
			{
				@Override
				public boolean apply(Class<?> input)
				{
					return input.getName().startsWith(pkgFilter);
				}
			};
		}

		@Override
		public Collection<Class<?>> getAnnotatedWith(Class<? extends Annotation> annotation)
		{
			return Collections2.filter(finder.getTypesAnnotatedWith(annotation), filter);
		}
	}
}
