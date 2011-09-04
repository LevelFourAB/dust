package se.l4.dust.core.internal.discovery;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import se.l4.dust.api.discovery.ClassDiscovery;
import se.l4.dust.api.discovery.DiscoveryFactory;

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
		
		public Set<String> getAnnotatedWith(Class<? extends Annotation> annotation)
		{
			return Collections.emptySet();
		}
	};
	
	private final List<ClassDiscovery> topLevel;
	
	public DiscoveryFactoryImpl()
	{
		topLevel = new CopyOnWriteArrayList<ClassDiscovery>();
	}
	
	public ClassDiscovery emtpy()
	{
		return EMPTY;
	}
	
	public ClassDiscovery get(String pkg)
	{
		return new FilteredDiscovery(topLevel, pkg);
	}
	
	public void addTopLevel(ClassDiscovery cd)
	{
		topLevel.add(cd);
	}
	
	private static class FilteredDiscovery
		implements ClassDiscovery
	{
		private final List<ClassDiscovery> topLevel;
		private final String pkg;

		public FilteredDiscovery(List<ClassDiscovery> topLevel, String pkg)
		{
			this.topLevel = topLevel;
			this.pkg = pkg == null ? null 
				: pkg.endsWith(".") ? pkg : pkg + ".";
		}

		public void index()
		{
			for(ClassDiscovery f : topLevel)
			{
				f.index();
			}
		}

		public Set<String> getAnnotatedWith(Class<? extends Annotation> annotation)
		{
			if(pkg == null)
			{
				return Collections.emptySet();
			}
			
			Set<String> result = new HashSet<String>();
			for(ClassDiscovery cd : topLevel)
			{
				for(String c : cd.getAnnotatedWith(annotation))
				{
					if(c.startsWith(pkg))
					{
						result.add(c);
					}
				}
			}
			
			return result;
		}
		
	}
}
