package se.l4.dust.core.internal.template.dom;

import java.net.URL;
import java.util.Arrays;
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
import se.l4.dust.api.template.spi.FragmentEncounter;
import se.l4.dust.api.template.spi.PropertySource;
import se.l4.dust.api.template.spi.TemplateBuilder;
import se.l4.dust.api.template.spi.TemplateFragment;
import se.l4.dust.api.template.spi.TemplateInfo;
import se.l4.dust.core.internal.template.components.EmittableComponent;
import se.l4.dust.core.internal.template.components.HolderComponent;
import se.l4.dust.core.internal.template.components.ParameterComponent;

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
	
	private final Map<String, Object> values;
	
	private final Integer id;
	
	private Class<?> context;
	
	private DocType docType;
	private Element current;
	private Element root;
	private URL url;
	private int line;
	private int column;

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
		this.id = templates.fetchTemplateId();
		
		values = new HashMap<String, Object>();
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
		return current instanceof Component || current instanceof FragmentElement;
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
		applyDebugHints(e);
		
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
		
		Element emittable;
		boolean fragment;
		if(EmittableComponent.class.isAssignableFrom(component))
		{
			emittable = (EmittableComponent) injector.getInstance(component);
			fragment = false;
		}
		else if(TemplateFragment.class.isAssignableFrom(component))
		{
			TemplateFragment f = (TemplateFragment) injector.getInstance(component);
			emittable = new FragmentElement(f);
			emittable.setParent(current);
			fragment = true;
		}
		else
		{
			emittable = new ClassTemplateComponent(component.getSimpleName(), injector, templateCache, converter, component);
			fragment = false;
		}
		
		applyDebugHints(emittable);
		
		if(emittable instanceof ClassTemplateComponent || emittable instanceof ParameterComponent)
		{
			// Insert a context switcher
			Element switcher = new DataContextSwitcher(id);
			emittable.addContent(switcher);
			
			if(current == null)
			{
				root = emittable;
				current = switcher;
			}
			else
			{
				current.addContent(emittable);
				current = switcher;
			}
			
			return this;
		}
		
		if(current == null)
		{
			// No current item, set as root
			root = emittable;
		}
		else if(! fragment)
		{
			// Non fragment, check if we need to wrap it
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
		
		if(current instanceof FragmentElement)
		{
			FragmentElement self = (FragmentElement) current;
			
			current = current.getParent();
			
			self.apply(this);
		}
		else
		{
			if(current instanceof DataContextSwitcher)
			{
				// First copy all attributes
				current.getParent().setAttributes(current.getAttributes());
				current.setAttributes(null);
				
				// Really looking for parent of parent
				current = current.getParent();
			}
			
			if(current instanceof ParameterComponent)
			{
				// Seek upwards to find a suitable ClassTemplateComponent
				Element parent = current.getParent();
				while(parent != null)
				{
					if(parent instanceof DataContextSwitcher)
					{
						Element cs = new ComponentContextSwitcher(id);
						cs.setContents(current.getRawContents());
						current.setContents(new Content[] { cs });
						cs.setParent(current);
						break;
					}
					
					parent = parent.getParent();
				}
			}
			
			current = current.getParent();
		}
		
		return this;
	}
	
	public TemplateBuilder endCurrent()
	{
		if(current == null)
		{
			throw new IllegalStateException("No current element or component");
		}
		
		if(isComponent())
		{
			endComponent();
		}
		else
		{
			endElement();
		}
		
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
		return applyDebugHints(new ExpressionContent(expr));
	}
	
	public TemplateBuilder comment(List<Content> content)
	{
		Comment comment = new Comment();
		comment.setParent(current);
		comment.addContent(content);
		
		applyDebugHints(comment);
		
		current.addContent(comment);
		
		return this;
	}
	
	public TemplateBuilder comment(String comment)
	{
		return comment(Collections.<Content>singletonList(new Text(comment)));
	}
	
	private <T extends Content> T applyDebugHints(T object)
	{
		object.withDebugInfo(url.getPath(), line, column);
		line = 0;
		column = 0;
		return object;
	}

	public Element getRootElement()
	{
		return root;
	}

	public ParsedTemplate getTemplate()
	{
		return new ParsedTemplate(url.getPath(), docType, root, id);
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
				mixin.element(new MixinEncounterImpl(boundNamespaces));
			}
		}
	}
	
	@Override
	public <T> T getValue(String id)
	{
		return (T) values.get(id);
	}
	
	@Override
	public void putValue(String id, Object value)
	{
		values.put(id, value);
	}
	
	@Override
	public TemplateBuilder addDebugHint(int line, int column)
	{
		this.line = line;
		this.column = column;
		return this;
	}
	
	private class MixinEncounterImpl
		implements MixinEncounter
	{
		private final Map<String, String> boundNamespaces;
		
		public MixinEncounterImpl(Map<String, String> ns)
		{
			boundNamespaces = new HashMap<String, String>(ns);
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
		
		@Override
		public void append(Content... content)
		{
			current.addContent(Arrays.asList(content));
		}
		
		@Override
		public void append(List<Content> content)
		{
			current.addContent(content);
		}
		
		@Override
		public void prepend(Content... content)
		{
			current.prependContent(Arrays.asList(content));
		}
		
		@Override
		public void prepend(List<Content> content)
		{
			current.prependContent(content);
		}
		
		@Override
		public MixinEncounter bindNamespace(String prefix, String uri)
		{
			boundNamespaces.put(prefix, uri);
			return this;
		}
		
		@Override
		public Content parseExpression(String expression)
		{
			Expression expr = expressions.compile(boundNamespaces, expression, context);
			return new ExpressionContent(expr);
		}
		
		@Override
		public Content parseExpression(String expression, Object context)
		{
			Expression expr = expressions.compile(boundNamespaces, expression, expressions.resolveType(context));
			return new ExpressionContentWithContext(expr, context);
		}
		
		@Override
		public void setAttribute(String attribute, Content content)
		{
			current.setAttribute(attribute, content);
		}
	}
	
	private static class FragmentElement
		extends Element
	{
		private final TemplateFragment fragment;

		public FragmentElement(TemplateFragment fragment)
		{
			super("internal:fragment:" + fragment.getClass().getSimpleName());
			
			this.fragment = fragment;
		}

		public void apply(final TemplateBuilderImpl builder)
		{
			final Element self = this;
			fragment.build(new FragmentEncounter()
			{
				@Override
				public Element.Attribute getAttribute(String namespace, String name)
				{
					// TODO
//					return self.getAttribute(namespace, name);
					return null;
				}

				@Override
				public Element.Attribute getAttribute(String name)
				{
					return self.getAttribute(name);
				}
				
				@Override
				public Element findParameter(String name)
				{
					for(Content c : self.getRawContents())
					{
						if(c instanceof ParameterComponent)
						{
							ParameterComponent pc = (ParameterComponent) c;
							Attribute attr = pc.getAttribute("name");
							if(attr != null && attr.getStringValue().equals(name))
							{
								return pc;
							}
						}
					}
					
					return null;
				}
				
				@Override
				public Content[] getBody()
				{
					return getRawContents();
				}
				
				@Override
				public TemplateBuilder builder()
				{
					return builder;
				}
				
				@Override
				public void replaceWith(Object component)
				{
					EmittableComponent emittable;
					if(component instanceof EmittableComponent)
					{
						emittable = (EmittableComponent) component;
					}
					else
					{
						emittable = new ClassTemplateComponent(
							component.getClass().getSimpleName(),
							builder.injector,
							builder.templateCache,
							builder.converter,
							component
						);
					}
					
					if(emittable instanceof ClassTemplateComponent || emittable instanceof ParameterComponent)
					{
						// Insert a context switcher
						Element switcher = new DataContextSwitcher(builder.id);
						emittable.addContent(switcher);
						
						if(builder.current == null)
						{
							builder.root = emittable;
						}
						else
						{
							builder.current.addContent(emittable);
						}
					}
					else
					{
						(builder.current == null ? builder.root : builder.current).addContent(emittable);
						if(builder.current == null)
						{
							builder.root = emittable;
						}
					}
				}
			});
		}
	}
}
