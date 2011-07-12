package se.l4.dust.jaxrs.internal;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.ws.rs.Path;

import org.scannotation.AnnotationDB;
import org.scannotation.ClasspathUrlFinder;
import org.scannotation.WarUrlFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import se.l4.dust.api.NamespaceManager;
import se.l4.dust.api.TemplateManager;
import se.l4.dust.api.annotation.Component;
import se.l4.dust.jaxrs.PageManager;

/**
 * Module that performs a single contribution scanning the classpath for
 * classes that are either pages or components. It then checks if they are
 * within a registered package and if so registers them automatically.
 * 
 * @author Andreas Holstenson
 *
 */
public class PageDiscovery
{
	private static final Logger logger = LoggerFactory.getLogger(PageDiscovery.class);
	private final NamespaceManager manager;
	private final PageManager pages;
	private final TemplateManager components;
	
	@Inject
	public PageDiscovery(
			NamespaceManager manager,
			PageManager pages,
			TemplateManager components)
	{
		this.manager = manager;
		this.pages = pages;
		this.components = components;
	}

	public void discover(ServletContext ctx)
		throws Exception
	{
		logger.info("Attempting to discover classes within registered namespaces");
		
		AnnotationDB db = new AnnotationDB();
		db.setScanClassAnnotations(true);
		db.setScanFieldAnnotations(false);
		db.setScanMethodAnnotations(false);
		db.setScanParameterAnnotations(false);
		
		db.scanArchives(ClasspathUrlFinder.findClassPaths());
		db.scanArchives(WarUrlFinder.findWebInfLibClasspaths(ctx));
		db.scanArchives(findClasspath().toArray(new URL[0]));
		
		Map<String, Set<String>> index = db.getAnnotationIndex();
		int p = handlePages(index);
		int c = handleComponents(index);
		
		logger.info("Found " + p + " pages and " + c + " components");
	}
	
	private List<URL> findClasspath()
		throws IOException
	{
		Enumeration<URL> enumeration = Thread.currentThread()
			.getContextClassLoader()
			.getResources("META-INF/MANIFEST.MF");
		
		List<URL> urls = new LinkedList<URL>();
		
		while(enumeration.hasMoreElements())
		{
			URL u = enumeration.nextElement();
			if(u.getProtocol().equals("jar"))
			{
				String url = u.toString();
				int idx = url.indexOf('!');
				String newUrl = url.substring(4, idx);
				urls.add(new URL(newUrl));
			}
		}
		
		return urls;
	}
	
	/**
	 * Handle all pages found (everything annotated with {@link Path}).
	 * 
	 * @param manager
	 * @param pages
	 * @param s
	 * @return
	 * @throws Exception
	 */
	private int handlePages(Map<String, Set<String>> s)
		throws Exception
	{
		int count = 0;
		Set<String> classes = s.get(Path.class.getName());
		if(classes != null)
		{
			for(String className : classes)
			{
				NamespaceManager.Namespace ns = findNamespace(className);
				if(ns != null)
				{
					// This class is handled so we register it
					pages.add(Class.forName(className));
					count++;
				}
			}
		}
		
		return count;
	}
	
	/**
	 * Handle all components (annotated with {@link Component}).
	 * 
	 * @param manager
	 * @param pages
	 * @param components
	 * @param s
	 * @return
	 * @throws Exception
	 */
	private int handleComponents(Map<String, Set<String>> s)
		throws Exception
	{
		int count = 0;
		Set<String> classes = s.get(Component.class.getName());
		if(classes != null)
		{
			for(String className : classes)
			{
				NamespaceManager.Namespace ns = findNamespace(className);
				if(ns != null)
				{
					// This class is handled so we register it
					components.getNamespace(ns.getUri())
						.addComponent(Class.forName(className));
					count++;
				}
			}
		}
		
		return count;
	}
	
	/**
	 * Try to find a registered namespace for a given class name by starting
	 * with its package and slowly reducing it downwards until either a match
	 * can be found or no more segments are available in the package.
	 * 
	 * <p>
	 * For example if we have the class {@code org.example.deep.pkg.TestClass}
	 * and a namespace registered for {@code org.example} the search would be
	 * {@code org.example.deep.pkg}, {@code org.example.deep} and finally
	 * {@code org.example}.
	 *  
	 * @param pages
	 * @param className
	 * @return
	 */
	private NamespaceManager.Namespace findNamespace(String className)
	{
		int idx = className.lastIndexOf('.');
		while(idx > 0)
		{
			className = className.substring(0, idx);
			
			NamespaceManager.Namespace ns = manager.getBinding(className);
			if(ns != null)
			{
				return ns;
			}
			
			idx = className.lastIndexOf('.');
		}
		
		return null;
	}
}
