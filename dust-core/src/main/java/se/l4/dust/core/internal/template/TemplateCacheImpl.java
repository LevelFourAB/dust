package se.l4.dust.core.internal.template;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import org.jdom.Content;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.Parent;
import org.jdom.input.SAXBuilder;
import org.jdom.input.SAXHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.MapMaker;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import se.l4.crayon.Environment;
import se.l4.dust.api.NamespaceManager;
import se.l4.dust.api.TemplateManager;
import se.l4.dust.api.annotation.Template;
import se.l4.dust.core.internal.template.dom.ContentPreload;
import se.l4.dust.core.internal.template.dom.ExpressionParser;
import se.l4.dust.core.internal.template.dom.TemplateFactory;
import se.l4.dust.core.internal.template.dom.TemplateSAXHandler;
import se.l4.dust.core.template.TemplateCache;
import se.l4.dust.dom.Document;
import se.l4.dust.dom.Element;

@Singleton
public class TemplateCacheImpl
	implements TemplateCache
{
	private static final Logger logger = LoggerFactory.getLogger(TemplateCacheImpl.class);
	
	private final SAXBuilder builder;
	private final TemplateManager manager;
	private final ExpressionParser expressionParser;
	private final InnerCache inner;
	
	private final NamespaceManager namespaces;
	
	@Inject
	public TemplateCacheImpl(
			final Provider<TemplateFactory> factory, 
			TemplateManager manager,
			NamespaceManager namespaces,
			ExpressionParser expressionPareser,
			Environment env)
	{
		this.manager = manager;
		this.namespaces = namespaces;
		this.expressionParser = expressionPareser;
		
		builder = new SAXBuilder()
		{
			@Override
			protected SAXHandler createContentHandler()
			{
				return new TemplateSAXHandler(factory.get());
			}
		};
		builder.setReuseParser(false);
//		builder.setFactory(factory);
		
		inner = env == Environment.DEVELOPMENT 
			? new DevelopmentCache()
			: new ProductionCache();
			
		logger.info("Template cache is in " + env + " mode");
	}
	
	/**
	 * Resolve a template based on a class and possibly an annotation. This
	 * is used internally to resolve a URL which used to load the template.
	 * 
	 * @param c
	 * @param annotation
	 * @return
	 * @throws IOException
	 */
	public Document getTemplate(Class<?> c, Template annotation)
		throws IOException
	{
		if(annotation != null)
		{
			return getTemplate0(c, annotation);
		}
		
		Class<?> current = c;
		while(current != Object.class)
		{
			Template t = current.getAnnotation(Template.class);
			if(t != null)
			{
				return getTemplate0(current, t);
			}
			
			current = current.getSuperclass();
		}
		
		return getTemplate(c, "");
	}

	private Document getTemplate0(Class<?> c, Template annotation)
		throws IOException
	{
		if(annotation.value() == Object.class)
		{
			return getTemplate(c, annotation.name());
		}
		else
		{
			return getTemplate(annotation.value(), annotation.name());
		}
	}
	
	private Document getTemplate(Class<?> c, String name)
		throws IOException
	{
		if(name.equals(""))
		{
			name = c.getSimpleName() + ".xml"; 
		}
		
		URL url = c.getResource(name);
		if(url == null)
		{
			throw new IOException("Could not find template " + name + " besides class " + c);
		}
		
		return getTemplate(url);
	}
	
	public Document getTemplate(URL url)
	{
		return inner.getTemplate(url);
	}
	
	private Document loadTemplate(URL url)
	{
		try
		{
			Document template = (Document) builder.build(url);
			preload(template);
			
			return template;
		}
		catch(JDOMException e)
		{
			throw new RuntimeException(e.getMessage(), e);
		}
		catch(IOException e)
		{
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	
	private void preload(Parent p)
		throws JDOMException
	{
		for(Content c : (List<Content>) p.getContent())
		{
			if(c instanceof ContentPreload)
			{
				((ContentPreload) c).preload(expressionParser);
			}
			
			if(c instanceof Parent)
			{
				preload((Parent) c);
			}
			
			if(c instanceof Element)
			{
				/*
				 * Remove component namespaces after the element has been
				 * preloaded, otherwise searching upwards for expressions
				 * is impossible
				 */
				Element e = (Element) c;
				
				List<Namespace> removable = new ArrayList<Namespace>(10);
				for(Namespace ns : e.getAdditionalNamespaces())
				{
					if(namespaces.isBound(ns) || manager.isComponentNamespace(ns))
					{
						removable.add(ns);
					}
				}
				
				for(Namespace ns : removable)
				{
					e.removeNamespaceDeclaration(ns);
				}
			}
		}
	}
	
	private interface InnerCache
	{
		Document getTemplate(URL url);
	}
	
	private class ProductionCache
		implements InnerCache
	{
		private final ConcurrentMap<URL, Document> templates;
		
		public ProductionCache()
		{
			templates = new MapMaker()
			.makeComputingMap(new Function<URL, Document>()
			{
				public Document apply(URL url)
				{
					return loadTemplate(url);
				}
			});
		}
		
		public Document getTemplate(URL url)
		{
			return templates.get(url);
		}
	}
	
	private class DevelopmentCache
		implements InnerCache
	{
		public Document getTemplate(URL url)
		{
			return loadTemplate(url);
		}
	}
}
