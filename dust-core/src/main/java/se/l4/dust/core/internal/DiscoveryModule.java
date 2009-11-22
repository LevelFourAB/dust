package se.l4.dust.core.internal;

import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.ws.rs.Path;

import org.jdom.Namespace;
import org.scannotation.AnnotationDB;
import org.scannotation.ClasspathUrlFinder;
import org.scannotation.WarUrlFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.l4.crayon.annotation.Contribution;
import se.l4.crayon.annotation.Order;
import se.l4.dust.api.NamespaceManager;
import se.l4.dust.api.PageManager;
import se.l4.dust.api.TemplateManager;
import se.l4.dust.api.annotation.Component;

/**
 * Module that performs a single contribution scanning the classpath for
 * classes that are either pages or components. It then checks if are
 * within a registered package and if so registers them automatically.
 * 
 * @author Andreas Holstenson
 *
 */
public class DiscoveryModule
{
	private static final Logger logger = LoggerFactory.getLogger(DiscoveryModule.class);
	
	@Contribution(name="discovery")
	@Order("last")
	public void discover(ServletContext ctx,
			NamespaceManager manager,
			PageManager pages,
			TemplateManager components)
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
		
		Map<String, Set<String>> index = db.getAnnotationIndex();
		int p = handlePages(manager, pages, index);
		int c = handleComponents(manager, pages, components, index);
		
		logger.info("Found " + p + " pages and " + c + " components");
	}
	
	private int handlePages(NamespaceManager manager, PageManager pages, Map<String, Set<String>> s)
		throws Exception
	{
		int count = 0;
		Set<String> classes = s.get(Path.class.getName());
		if(classes != null)
		{
			for(String className : classes)
			{
				Namespace ns = findNamespace(manager, className);
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
	
	private int handleComponents(NamespaceManager manager, PageManager pages, TemplateManager components, Map<String, Set<String>> s)
		throws Exception
	{
		int count = 0;
		Set<String> classes = s.get(Component.class.getName());
		if(classes != null)
		{
			for(String className : classes)
			{
				Namespace ns = findNamespace(manager, className);
				if(ns != null)
				{
					// This class is handled so we register it
					components.addComponent(ns, Class.forName(className));
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
	private Namespace findNamespace(NamespaceManager manager, String className)
	{
		int idx = className.lastIndexOf('.');
		while(idx > 0)
		{
			className = className.substring(0, idx);
			
			Namespace ns = manager.getBinding(className);
			if(ns != null)
			{
				return ns;
			}
			
			idx = className.lastIndexOf('.');
		}
		
		return null;
	}
}
