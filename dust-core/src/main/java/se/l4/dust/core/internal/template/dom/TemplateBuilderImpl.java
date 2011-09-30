package se.l4.dust.core.internal.template.dom;

import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import se.l4.dust.api.NamespaceManager;
import se.l4.dust.api.TemplateManager;
import se.l4.dust.api.TemplateManager.TemplateNamespace;
import se.l4.dust.api.conversion.TypeConverter;
import se.l4.dust.api.expression.Expression;
import se.l4.dust.api.expression.Expressions;
import se.l4.dust.api.template.TemplateCache;
import se.l4.dust.api.template.dom.Comment;
import se.l4.dust.api.template.dom.Component;
import se.l4.dust.api.template.dom.Content;
import se.l4.dust.api.template.dom.DocType;
import se.l4.dust.api.template.dom.Element;
import se.l4.dust.api.template.dom.Element.Attribute;
import se.l4.dust.api.template.dom.ParsedTemplate;
import se.l4.dust.api.template.dom.Text;
import se.l4.dust.api.template.dom.WrappedElement;
import se.l4.dust.api.template.mixin.ElementWrapper;
import se.l4.dust.api.template.mixin.MixinEncounter;
import se.l4.dust.api.template.mixin.TemplateMixin;
import se.l4.dust.api.template.spi.PropertySource;
import se.l4.dust.api.template.spi.TemplateBuilder;
import se.l4.dust.api.template.spi.TemplateInfo;
import se.l4.dust.core.internal.template.components.EmittableComponent;
import se.l4.dust.core.internal.template.components.HolderComponent;

import com.google.inject.Inject;
import com.google.inject.Injector;

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
	private final NamespaceManager namespaceManager;
	
	private final TemplateCache templateCache;
	private final TypeConverter converter;
	
	private final Map<String, String> boundNamespaces;
	private final TemplateInfo namespaces;
	private final Expressions expressions;
	
	private final LinkedList<Map<String, Element.Attribute>> mixinAttributes;
	
	private Class<?> context;
	
	private DocType docType;
	private Element current;
	private Element root;
	private URL url;

	@Inject
	public TemplateBuilderImpl(Injector injector, 
			NamespaceManager namespaceManager, 
			TemplateManager templates,
			TemplateCache templateCache,
			TypeConverter converter,
			Expressions expressions)
	{
		this.injector = injector;
		this.namespaceManager = namespaceManager;
		this.templates = templates;
		this.templateCache = templateCache;
		this.converter = converter;
		this.expressions = expressions;
		
		boundNamespaces = new HashMap<String, String>();
		mixinAttributes = new LinkedList<Map<String, Element.Attribute>>();
		namespaces = createNamespaces();
	}
	
	private TemplateInfo createNamespaces()
	{
		return new TemplateInfo()
		{
			public String getURL()
			{
				return url.toExternalForm();
			}
			
			public NamespaceManager.Namespace getNamespaceByPrefix(String prefix)
			{
				String uri = boundNamespaces.get(prefix);
				return uri == null ? null
					: namespaceManager.getNamespaceByURI(uri);
			}
		};
	}

	public void setContext(URL url, Class<?> context)
	{
		this.url = url;
		this.context = context;
	}
	
	public TemplateBuilder bindNamespace(String prefix, String uri)
	{
		boundNamespaces.put(prefix, uri);
		
		return this;
	}
	
	public TemplateBuilder unbindNamespace(String prefix)
	{
		boundNamespaces.remove(prefix);
		
		return this;
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
		
		// Add mixin attributes to the stack
		mixinAttributes.add(new LinkedHashMap<String, Element.Attribute>());
		
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
		
		applyMixins();
		
		mixinAttributes.removeLast();
		
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
			NamespaceManager.Namespace ns = namespaceManager.getNamespaceByPrefix(prefix);
			if(ns == null)
			{
				throw new IllegalArgumentException("No namespace bound to " + prefix);
			}
			
			TemplateManager.TemplateNamespace tpl = templates.getNamespace(ns.getUri());
			Class<?> component = tpl.getComponent(name);
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
		TemplateManager.TemplateNamespace tpl = templates.getNamespace(namespace);
		Class<?> component = tpl.getComponent(name);
		if(component == null)
		{
			throw new IllegalArgumentException("The component " + name + " could not be found in namespace " + namespace);
		}
		
		return startComponent(component);
	}
	
	public TemplateBuilder startComponent(Class<?> component)
	{
		// Add mixin attributes to the stack
		mixinAttributes.add(new LinkedHashMap<String, Element.Attribute>());
		
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
		
		applyMixins();
		
		mixinAttributes.removeLast();
		
		current = current.getParent();
		
		return this;
	}
	
	public TemplateBuilder endCurrent()
	{
		if(current == null)
		{
			throw new IllegalStateException("No current element or component");
		}
		
		applyMixins();
		
		current = current.getParent();
		
		mixinAttributes.removeLast();
		
		return this;
	}
	
	public TemplateBuilder setAttribute(String name, String value)
	{
		setAttribute(name, new Text(value));
		
		return this;
	}
	
	public TemplateBuilder setAttribute(String name, String value,
			boolean expand)
	{
		setAttribute(name, new Text(value));
		
		return this;
	}
	
	public TemplateBuilder setAttribute(String name, Content... value)
	{
		Element current = current();
		
		int idx = name.indexOf(':');
		if(idx > 0)
		{
			// Might be a bound namespace
			String prefix = name.substring(0, idx);
			String uri = boundNamespaces.get(prefix);
			if(uri == null)
			{
				// TODO: Throw exception on unbound ns?
			}
			else if(namespaceManager.isBound(uri))
			{
				// Bound attribute, store for usage within mixins
				mixinAttributes.getLast()
					.put(
						uri + "|" + name.substring(idx+1), 
						new Element.Attribute(name, value)
					);
				
				return this;
			}
		}
		
		current.setAttribute(name, value);
		
		return this;
	}
	
	public TemplateBuilder setAttribute(String name, List<Content> value)
	{
		setAttribute(name, value.toArray(new Content[value.size()]));
		
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
		if(prefix != null)
		{
			PropertySource source = templates.getPropertySource(prefix);
			if(source != null)
			{
				// TODO: Find the property parent
				return source.getPropertyContent(namespaces, context, expression);
			}
			
			// Join expression again
			expression = prefix + ":" + expression;
		}
		
		Expression expr = expressions.compile(boundNamespaces, expression, context);
		return new ExpressionContent(expr);
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
	
	private void applyMixins()
	{
		Map<String, Attribute> attrs = mixinAttributes.getLast();
		for(String attr : attrs.keySet())
		{
			int index = attr.indexOf('|');
			String nsUri = attr.substring(0, index);
			String name = attr.substring(index+1);
			
			TemplateNamespace namespace = templates.getNamespace(nsUri);
			if(namespace.hasMixin(name))
			{
				TemplateMixin mixin = namespace.getMixin(name);
				mixin.element(new MixinEncounterImpl());
			}
		}
	}
	
	private class MixinEncounterImpl
		implements MixinEncounter
	{
		public MixinEncounterImpl()
		{
		}

		@Override
		public Element.Attribute getAttribute(String namespace, String name)
		{
			return mixinAttributes.getLast().get(namespace + "|" + name);
		}

		@Override
		public Element.Attribute getAttribute(String name)
		{
			return current.getAttribute(name);
		}

		@Override
		public void wrap(ElementWrapper wrapper)
		{
			WrappedElement wrapped = new WrappedElement(current, wrapper);
			System.out.println("Current is " + current);
			if(current.getParent() == null)
			{
				// Special case, wrapping the root element
				HolderComponent holder = new HolderComponent();
				holder.addContent(wrapped);
				root = current = holder;
			}
			else
			{
				current.getParent()
					.replaceContent(current, wrapped);
			}
		}
		
	}
}
