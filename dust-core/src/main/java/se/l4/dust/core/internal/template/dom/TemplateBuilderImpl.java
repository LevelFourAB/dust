package se.l4.dust.core.internal.template.dom;

import java.util.Collections;
import java.util.List;

import org.jdom.Namespace;

import com.google.inject.Inject;
import com.google.inject.Injector;

import se.l4.dust.api.NamespaceManager;
import se.l4.dust.api.TemplateException;
import se.l4.dust.api.TemplateManager;
import se.l4.dust.api.conversion.TypeConverter;
import se.l4.dust.api.template.PropertySource;
import se.l4.dust.api.template.TemplateCache;
import se.l4.dust.api.template.dom.Comment;
import se.l4.dust.api.template.dom.Component;
import se.l4.dust.api.template.dom.Content;
import se.l4.dust.api.template.dom.DocType;
import se.l4.dust.api.template.dom.Element;
import se.l4.dust.api.template.dom.ParsedTemplate;
import se.l4.dust.api.template.dom.Text;
import se.l4.dust.api.template.spi.TemplateBuilder;
import se.l4.dust.core.internal.template.components.EmittableComponent;

/**
 * Implementation of {@link TemplateBuilder}.
 * 
 * @author Andreas Holstenson
 *
 */
public class TemplateBuilderImpl
	implements TemplateBuilder
{
	private final Injector injector;
	private final TemplateManager templates;
	private final NamespaceManager namespaces;
	
	private final TemplateCache templateCache;
	private final TypeConverter converter;
	
	private Class<?> context;
	
	private DocType docType;
	private Element current;
	private Element root;

	@Inject
	public TemplateBuilderImpl(Injector injector, 
			NamespaceManager namespaces, 
			TemplateManager templates,
			TemplateCache templateCache,
			TypeConverter converter)
	{
		this.injector = injector;
		this.namespaces = namespaces;
		this.templates = templates;
		this.templateCache = templateCache;
		this.converter = converter;
	}
	
	public void setContext(Class<?> context)
	{
		this.context = context;
	}
	
	public TemplateBuilder setDoctype(String name, String publicId,
			String systemId)
	{
		docType = new DocType(name, publicId, systemId);
		
		return this;
	}
	
	private Element current()
	{
		if(current == null)
		{
			throw new IllegalStateException("No current element");
		}
		
		return current;
	}
	
	private boolean isComponent()
	{
		return current instanceof Component;
	}
	
	public TemplateBuilder startElement(String name, String... attributes)
	{
		if(attributes.length % 2 != 0)
		{
			throw new IllegalArgumentException("Attributes must be given as key, value (in pairs)");
		}
		
		Element e = new Element(name, attributes);
		if(current == null)
		{
			root = e;
		}
		else
		{
			current.addContent(e);
		}
		
		current = e;
		
		return this;
	}
	
	public TemplateBuilder endElement()
	{
		if(current == null)
		{
			throw new IllegalStateException("No current element");
		}
		
		if(isComponent())
		{
			throw new IllegalStateException("Currently building a component: " + current);
		}
		
		current = current.getParent();
		
		return this;
	}
	
	public TemplateBuilder startComponent(String name)
	{
		int idx = name.indexOf(':');
		if(idx >= 0)
		{
			String prefix = name.substring(0, idx);
			name = name.substring(idx+1);
			Namespace ns = namespaces.getNamespaceByPrefix(prefix);
			if(ns == null)
			{
				throw new IllegalArgumentException("No namespace bound to " + prefix);
			}
			
			Class<?> component = templates.getComponent(ns, name);
			if(component == null)
			{
				throw new IllegalArgumentException("The component " + name + " could not be found in namespace " + prefix);
			}
			
			return startComponent(component);
		}
		else
		{
			throw new IllegalArgumentException("The given component does not have a namespace, consider using startComponent(name, namespace)");
		}
	}
	
	public TemplateBuilder startComponent(String name, String namespace)
	{
		Class<?> component = templates.getComponent(Namespace.getNamespace(namespace), name);
		if(component == null)
		{
			throw new IllegalArgumentException("The component " + name + " could not be found in namespace " + namespace);
		}
		
		return startComponent(component);
	}
	
	public TemplateBuilder startComponent(Class<?> component)
	{
		EmittableComponent emittable;
		if(EmittableComponent.class.isAssignableFrom(component))
		{
			emittable = (EmittableComponent) injector.getInstance(component);
		}
		else
		{
			emittable = new ClassTemplateComponent("", injector, templateCache, converter, component);
		}
		
		if(current == null)
		{
			root = emittable;
		}
		else
		{
			current.addContent(emittable);
		}
		
		current = emittable;
		
		return this;
	}
	
	public TemplateBuilder endComponent()
	{
		if(current == null)
		{
			throw new IllegalStateException("No current component");
		}
		
		if(false == isComponent())
		{
			throw new IllegalStateException("Currently building an element: " + current);
		}
		
		current = current.getParent();
		
		return this;
	}
	
	public TemplateBuilder endCurrent()
	{
		if(current == null)
		{
			throw new IllegalStateException("No current element or component");
		}
		
		current = current.getParent();
		
		return this;
	}
	
	public TemplateBuilder setAttribute(String name, String value)
	{
		current().setAttribute(name, new Text(value));
		
		return this;
	}
	
	public TemplateBuilder setAttribute(String name, String value,
			boolean expand)
	{
		current().setAttribute(name, new Text(value));
		
		return this;
	}
	
	public TemplateBuilder setAttribute(String name, Content... value)
	{
		current().setAttribute(name, value);
		
		return this;
	}
	
	public TemplateBuilder setAttribute(String name, List<Content> value)
	{
		current().setAttribute(name, value.toArray(new Content[value.size()]));
		
		return this;
	}
	
	public TemplateBuilder addContent(List<Content> content)
	{
		current().addContent(content);
		
		return this;
	}
	
	public Content createDynamicContent(String expression)
	{
		return createDynamicContent(null, expression);
	}
	
	public Content createDynamicContent(String prefix, String expression)
	{
		if(prefix == null)
		{
			prefix = "mvel";
		}
		
		PropertySource source = templates.getPropertySource(prefix);
		if(source == null)
		{
			throw new TemplateException("No property source found for " + prefix);
		}
		
		// TODO: Find the property parent
		return source.getPropertyContent(context, expression, current);
	}
	
	public TemplateBuilder comment(List<Content> content)
	{
		Comment comment = new Comment();
		comment.setParent(current);
		comment.addContent(content);
		
		current.addContent(comment);
		
		return this;
	}
	
	public TemplateBuilder comment(String comment)
	{
		return comment(Collections.<Content>singletonList(new Text(comment)));
	}

	public Element getRootElement()
	{
		return root;
	}

	public ParsedTemplate getTemplate()
	{
		return new ParsedTemplate(docType, root);
	}
	
	public boolean hasCurrent()
	{
		return current != null;
	}
}
