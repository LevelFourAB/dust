package se.l4.dust.core.internal.discovery;

import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.ReflectionsException;
import org.reflections.Store;
import org.reflections.scanners.Scanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.reflections.vfs.Vfs;

import se.l4.dust.api.discovery.ClassDiscovery;
import se.l4.dust.api.discovery.DiscoveryFactory;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.MapMaker;
import com.google.inject.Singleton;

/**
 * Implementation of {@link DiscoveryFactory}.
 * 
 * @author Andreas Holstenson
 *
 */
@Singleton
public class DiscoveryFactoryImpl
	implements DiscoveryFactory
{
	private static final ClassDiscovery EMPTY = new ClassDiscovery()
	{
		public void index()
		{
		}
		
		public Set<Class<?>> getAnnotatedWith(Class<? extends Annotation> annotation)
		{
			return Collections.emptySet();
		}
	};
	
	private final Map<String, ClassDiscovery> packages;
	
	public DiscoveryFactoryImpl()
	{
		packages = new MapMaker()
			.makeComputingMap(new Function<String, ClassDiscovery>()
			{
				public ClassDiscovery apply(String input)
				{
					return new ClassDiscoveryImpl(input);
				}
			});
	}
	
	public ClassDiscovery empty()
	{
		return EMPTY;
	}
	
	public ClassDiscovery get(String pkg)
	{
		return pkg == null ? empty() : packages.get(pkg);
	}
	
	private static class ClassDiscoveryImpl
		implements ClassDiscovery
	{
		private final String pkg;
		private final ConfigurationBuilder configuration;
		private final Store store;

		public ClassDiscoveryImpl(String pkg)
		{
			this.pkg = pkg;
			
			configuration = new ConfigurationBuilder();
			final Predicate<String> filter = new FilterBuilder.Include(FilterBuilder.prefix(pkg));

			configuration.setUrls(ClasspathHelper.forPackage(pkg));
			configuration.filterInputsBy(filter);
			configuration.setScanners(
				new TypeAnnotationsScanner().filterResultsBy(filter)
			);
			
			store = new Store(configuration);
			for(Scanner scanner : configuration.getScanners())
			{
				scanner.setConfiguration(configuration);
				scanner.setStore(store.get(scanner));
			}
			
			index();
		}

		public void index()
		{
			for(URL url : configuration.getUrls())
			{
				try
				{
					for(final Vfs.File file : Vfs.fromURL(url).getFiles())
					{
						String input = file.getRelativePath().replace('/', '.');
						if(configuration.acceptsInput(input))
						{
							for(Scanner scanner : configuration.getScanners())
							{
								try
								{
									if(scanner.acceptsInput(input))
									{
										scanner.scan(file);
									}
								}
								catch(Exception e)
								{
								}
							}
						}
					}
				}
				catch(ReflectionsException e)
				{
				}
			}
		}

		public Set<Class<?>> getAnnotatedWith(Class<? extends Annotation> annotation)
		{
			Set<String> result = store.getTypesAnnotatedWith(annotation.getName());
			return ImmutableSet.copyOf(Reflections.forNames(result));
		}
		
	}
}
