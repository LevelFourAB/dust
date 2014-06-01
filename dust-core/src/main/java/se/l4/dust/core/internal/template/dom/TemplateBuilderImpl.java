package se.l4.dust.core.internal.template.dom;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import se.l4.dust.api.Namespace;
import se.l4.dust.api.Namespaces;
import se.l4.dust.api.conversion.TypeConverter;
import se.l4.dust.api.expression.Expression;
import se.l4.dust.api.expression.Expressions;
import se.l4.dust.api.template.Emittable;
import se.l4.dust.api.template.TemplateBuilder;
import se.l4.dust.api.template.TemplateCache;
import se.l4.dust.api.template.TemplateException;
import se.l4.dust.api.template.Templates;
import se.l4.dust.api.template.Templates.TemplateNamespace;
import se.l4.dust.api.template.dom.Comment;
import se.l4.dust.api.template.dom.Content;
import se.l4.dust.api.template.dom.DocType;
import se.l4.dust.api.template.dom.Element;
import se.l4.dust.api.template.dom.Element.Attribute;
import se.l4.dust.api.template.dom.ParsedTemplate;
import se.l4.dust.api.template.dom.Text;
import se.l4.dust.api.template.dom.WrappedElement;
import se.l4.dust.api.template.fragment.FragmentEncounter;
import se.l4.dust.api.template.fragment.TemplateFragment;
import se.l4.dust.api.template.mixin.ElementWrapper;
import se.l4.dust.api.template.mixin.MixinEncounter;
import se.l4.dust.api.template.mixin.TemplateMixin;
import se.l4.dust.api.template.spi.ErrorCollector;
import se.l4.dust.api.template.spi.TemplateInfo;

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
	private final Templates templates;
	private final Namespaces namespaceManager;
	
	private final TemplateCache templateCache;
	private final TypeConverter converter;
	
	private final Map<String, String> boundNamespaces;
	private final TemplateInfo namespaces;
	private final Expressions expressions;
	
	private final LinkedList<Map<String, Element.Attribute>> mixinAttributes;
	
	private final Map<String, Object> values;
	
	private final Integer id;
	
	private ErrorCollector errorCollector;
	private Class<?> context;
	
	private DocType docType;
	private Element current;
	private Element root;
	private URL url;
	private int line;
	private int column;

	@Inject
	public TemplateBuilderImpl(Injector injector, 
			Namespaces namespaceManager, 
			Templates templates,
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
			@Override
			public String getURL()
			{
				return url.toExternalForm();
			}
			
			@Override
			public Namespace getNamespaceByPrefix(String prefix)
			{
				String uri = boundNamespaces.get(prefix);
				return uri == null ? null
					: namespaceManager.getNamespaceByURI(uri);
			}
		};
	}
	
	private TemplateException raiseError(String message)
	{
		errorCollector.newError(line, column, message);
		return errorCollector.raiseException();
	}
	
	@Override
	public TemplateBuilder withErrorCollector(ErrorCollector collector)
	{
		this.errorCollector = collector;
		return this;
	}

	public void setContext(URL url, Class<?> context)
	{
		this.url = url;
		this.context = context;
	}
	
	@Override
	public TemplateBuilder bindNamespace(String prefix, String uri)
	{
		boundNamespaces.put(prefix, uri);
		
		return this;
	}
	
	@Override
	public TemplateBuilder unbindNamespace(String prefix)
	{
		boundNamespaces.remove(prefix);
		
		return this;
	}
	
	@Override
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
			throw raiseError("No current element");
		}
		
		return current;
	}
	
	private boolean isFragment()
	{
		return current instanceof FragmentElement;
	}
	
	@Override
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
	
	@Override
	public TemplateBuilder endElement()
	{
		if(current == null)
		{
			throw raiseError("No current element");
		}
		
		if(isFragment())
		{
			throw raiseError("Currently building a fragment: " + current);
		}
		
		applyMixins();
		
		mixinAttributes.removeLast();
		
		current = current.getParent();
		
		return this;
	}
	
	@Override
	public TemplateBuilder startFragment(String name)
	{
		int idx = name.indexOf(':');
		if(idx >= 0)
		{
			String prefix = name.substring(0, idx);
			name = name.substring(idx+1);
			Namespace ns = namespaceManager.getNamespaceByPrefix(prefix);
			if(ns == null)
			{
				throw new IllegalArgumentException("No namespace bound to " + prefix);
			}
			
			Templates.TemplateNamespace tpl = templates.getNamespace(ns.getUri());
			TemplateFragment fragment = tpl.getFragment(name);
			if(fragment == null)
			{
				throw new IllegalArgumentException(name + " could not be found in namespace " + prefix);
			}
			
			return startFragment(fragment);
		}
		else
		{
			throw new IllegalArgumentException("The given fragment does not have a namespace, consider using startFragment(name, namespace)");
		}
	}
	
	@Override
	public TemplateBuilder startFragment(String name, String namespace)
	{
		Templates.TemplateNamespace tpl = templates.getNamespace(namespace);
		TemplateFragment fragment = tpl.getFragment(name);
		if(fragment == null)
		{
			throw new IllegalArgumentException(name + " could not be found in namespace " + namespace);
		}
		
		return startFragment(fragment);
	}
	
	@Override
	public TemplateBuilder startFragment(TemplateFragment fragment)
	{
		// Add mixin attributes to the stack
		mixinAttributes.add(new LinkedHashMap<String, Element.Attribute>());
				
		Element emittable = new FragmentElement(fragment);
		emittable.setParent(current);
		
		applyDebugHints(emittable);
		
		if(current == null)
		{
			// No current item, set as root
			root = emittable;
		}
		
		current = emittable;
		
		return this;
	}
	
	@Override
	public TemplateBuilder endFragment()
	{
		if(! isFragment())
		{
			throw raiseError("Currently building an element: " + current);
		}
		
		FragmentElement self = (FragmentElement) current;
		
		current = current.getParent();
		
		self.apply(this);
		
		if(self.wasReplaced())
		{
			applyMixins();
			
			mixinAttributes.removeLast();
			
			current = current.getParent();
		}
		
		return this;
	}
	
	@Override
	public TemplateBuilder endCurrent()
	{
		if(current == null)
		{
			throw raiseError("No current element or component");
		}
		
		if(isFragment())
		{
			endFragment();
		}
		else
		{
			endElement();
		}
		
		return this;
	}
	
	@Override
	public TemplateBuilder setAttribute(String name, String value)
	{
		setAttribute(name, new Text(value));
		
		return this;
	}
	
	@Override
	public TemplateBuilder setAttribute(String name, String value,
			boolean expand)
	{
		setAttribute(name, new Text(value));
		
		return this;
	}
	
	@Override
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
	
	@Override
	public TemplateBuilder setAttribute(String name, List<Content> value)
	{
		setAttribute(name, value.toArray(new Content[value.size()]));
		
		return this;
	}
	
	@Override
	public TemplateBuilder addContent(List<Content> content)
	{
		current().addContent(content);
		
		return this;
	}
	
	@Override
	public Content createDynamicContent(String expression)
	{
		Expression expr = expressions.compile(url, boundNamespaces, expression, context);
		return applyDebugHints(new ExpressionContent(expr));
	}
	
	@Override
	public TemplateBuilder comment(List<Content> content)
	{
		Comment comment = new Comment();
		comment.setParent(current);
		comment.addContent(content);
		
		applyDebugHints(comment);
		
		current.addContent(comment);
		
		return this;
	}
	
	@Override
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
		return new ParsedTemplate(url.toString(), context.getSimpleName(), docType, root, id);
	}
	
	@Override
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
				Empty empty = new Empty();
				empty.addContent(wrapped);
				root = current = empty;
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
			Expression expr = expressions.compile(url, boundNamespaces, expression, context);
			return new ExpressionContent(expr);
		}
		
		@Override
		public Content parseExpression(String expression, Object context)
		{
			Expression expr = expressions.compile(url, boundNamespaces, expression, expressions.resolveType(context));
			return new ExpressionContentWithContext(expr, context);
		}
		
		@Override
		public void setAttribute(String attribute, Content content)
		{
			current.setAttribute(attribute, content);
		}
		
		@Override
		public void error(String error)
		{
			errorCollector.newError(line, column, error);
		}
	}
	
	private static class FragmentElement
		extends Element
	{
		private final TemplateFragment fragment;
		private boolean replaced;

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
				public Attribute getAttribute(String name, boolean required)
				{
					Attribute attribute = self.getAttribute(name);
					if(attribute == null)
					{
						raiseError("The attribute " + name + " is required but was not found");
					}
					return attribute;
				}
				
				@Override
				public Element findParameter(String name)
				{
					return self.getParameter(name);
				}
				
				@Override
				public void addParameter(String name, Element content)
				{
					if(self.getParent() != null)
					{
						self.getParent().addParameter(name, content);
					}
				}
				
				@Override
				public void raiseError(String message)
				{
					throw builder.raiseError(message);
				}
				
				@Override
				public Content[] getBody()
				{
					return getRawContents();
				}
				
				@Override
				public Element getScopedBody()
				{
					return new EmittableContent(new DataContextSwitcher(builder.id, self.getRawContents()));
				}
				
				@Override
				public TemplateBuilder builder()
				{
					return builder;
				}
				
				@Override
				public void replaceWith(Emittable emittable)
				{
					Element content = new EmittableContent(emittable);
					
					if(builder.current == null)
					{
						builder.root = content;
					}
					else
					{
						builder.current.addContent(content);
					}
					
					builder.current = content;
					
					replaced = true;
				}
				
				@Override
				public void replaceWith(Content[] content)
				{
					Element root;
					if(builder.current == null)
					{
						root = builder.root = new Empty();
					}
					else
					{
						root = builder.current;
					}
					
					for(Content c : content)
					{
						root.addContent(c);
					}
					
					builder.current = root;
					
					replaced = true;
				}
				
				@Override
				public void replaceWith(final Iterable<Content> content)
				{
					Element root;
					if(builder.current == null)
					{
						root = builder.root = new Empty();
					}
					else
					{
						root = builder.current;
					}
					
					for(Content c : content)
					{
						root.addContent(c);
					}
					
					builder.current = root;
					
					replaced = true;
				}
			});
		}
		
		public boolean wasReplaced()
		{
			return replaced;
		}
	}
}
