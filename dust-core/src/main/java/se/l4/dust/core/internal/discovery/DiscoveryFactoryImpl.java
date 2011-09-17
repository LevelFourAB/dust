package se.l4.dust.core.internal.discovery;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;

import se.l4.dust.api.discovery.ClassDiscovery;
import se.l4.dust.api.discovery.DiscoveryFactory;

import com.google.common.base.Function;
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
		private volatile Reflections reflections;

		public ClassDiscoveryImpl(String pkg)
		{
			this.pkg = pkg;
			reflections = new Reflections(pkg);
		}

		public void index()
		{
			reflections = new Reflections(pkg);
		}

		public Set<Class<?>> getAnnotatedWith(Class<? extends Annotation> annotation)
		{
			return reflections.getTypesAnnotatedWith(annotation);
		}
		
	}
}
