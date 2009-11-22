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

import se.l4.dust.api.TemplateManager;
import se.l4.dust.core.internal.template.dom.ContentPreload;
import se.l4.dust.core.internal.template.dom.TemplateFactory;
import se.l4.dust.core.internal.template.dom.TemplateSAXHandler;
import se.l4.dust.core.template.TemplateCache;
import se.l4.dust.dom.Document;
import se.l4.dust.dom.Element;

import com.google.common.base.Function;
import com.google.common.collect.MapMaker;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class TemplateCacheImpl
	implements TemplateCache
{
	private final ConcurrentMap<URL, Document> templates;
	private final SAXBuilder builder;
	private final TemplateManager manager;
	
	@Inject
	public TemplateCacheImpl(final TemplateFactory factory, TemplateManager manager)
	{
		this.manager = manager;
		
		builder = new SAXBuilder()
		{
			@Override
			protected SAXHandler createContentHandler()
			{
				return new TemplateSAXHandler(factory);
			}
		};
		builder.setReuseParser(false);
//		builder.setFactory(factory);
		
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
	
	private void preload(Parent p) throws JDOMException
	{
		for(Content c : (List<Content>) p.getContent())
		{
			if(c instanceof ContentPreload)
			{
				((ContentPreload) c).preload();
			}
			
			if(c instanceof Element)
			{
				Element e = (Element) c;
				
				// Remove component namespaces
				List<Namespace> removable = new ArrayList<Namespace>(10);
				for(Namespace ns : e.getAdditionalNamespaces())
				{
					if(manager.isComponentNamespace(ns))
					{
						removable.add(ns);
					}
				}
				
				for(Namespace ns : removable)
				{
					e.removeNamespaceDeclaration(ns);
				}
			}
			
			if(c instanceof Parent)
			{
				preload((Parent) c);
			}
		}
	}
}
