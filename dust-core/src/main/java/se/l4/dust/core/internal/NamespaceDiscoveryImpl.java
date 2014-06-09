package se.l4.dust.core.internal;

import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import se.l4.dust.api.Namespace;
import se.l4.dust.api.Namespaces;
import se.l4.dust.api.discovery.DiscoveryEncounter;
import se.l4.dust.api.discovery.DiscoveryHandler;
import se.l4.dust.api.discovery.NamespaceDiscovery;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;

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
		Set<URL> urls = Sets.newHashSet();
		FilterBuilder builder = new FilterBuilder();
		for(Namespace ns : namespaces.list())
		{
			if(ns.getPackage() == null || ns.getPackage().isEmpty()) continue;
			
			urls.addAll(ClasspathHelper.forPackage(ns.getPackage()));
			builder.include(FilterBuilder.prefix(ns.getPackage() + "."));
		}
		
		ConfigurationBuilder configuration = new ConfigurationBuilder();

		configuration.setUrls(urls);
		configuration.filterInputsBy(builder);
		configuration.setScanners(
			new TypeAnnotationsScanner()
		);
		
		Reflections reflections = new Reflections(configuration);
		DiscoveryEncounterImpl encounter = new DiscoveryEncounterImpl(reflections);
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
		private final Reflections reflections;
		private Predicate<Class<?>> filter;
		
		public DiscoveryEncounterImpl(Reflections reflections)
		{
			this.reflections = reflections;
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
			return Collections2.filter(reflections.getTypesAnnotatedWith(annotation), filter);
		}
	}
}
