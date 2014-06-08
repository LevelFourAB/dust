package se.l4.dust.core.internal.template;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import se.l4.dust.api.Namespace;
import se.l4.dust.api.Namespaces;
import se.l4.dust.api.Value;
import se.l4.dust.api.Values;
import se.l4.dust.api.conversion.TypeConverter;
import se.l4.dust.api.expression.Expression;
import se.l4.dust.api.expression.Expressions;
import se.l4.dust.api.resource.ResourceLocation;
import se.l4.dust.api.template.Emittable;
import se.l4.dust.api.template.TemplateBuilder;
import se.l4.dust.api.template.TemplateEmitter;
import se.l4.dust.api.template.TemplateException;
import se.l4.dust.api.template.TemplateOutputStream;
import se.l4.dust.api.template.Templates;
import se.l4.dust.api.template.Templates.TemplateNamespace;
import se.l4.dust.api.template.dom.Attribute;
import se.l4.dust.api.template.dom.AttributeImpl;
import se.l4.dust.api.template.dom.Comment;
import se.l4.dust.api.template.dom.Content;
import se.l4.dust.api.template.dom.DocType;
import se.l4.dust.api.template.dom.Element;
import se.l4.dust.api.template.dom.HtmlElement;
import se.l4.dust.api.template.dom.ParsedTemplate;
import se.l4.dust.api.template.dom.Text;
import se.l4.dust.api.template.dom.WrappedElement;
import se.l4.dust.api.template.fragment.FragmentEncounter;
import se.l4.dust.api.template.fragment.TemplateFragment;
import se.l4.dust.api.template.mixin.ElementWrapper;
import se.l4.dust.api.template.mixin.MixinEncounter;
import se.l4.dust.api.template.mixin.TemplateMixin;
import se.l4.dust.api.template.spi.ErrorCollector;
import se.l4.dust.core.internal.template.components.DataContextSwitcher;
import se.l4.dust.core.internal.template.dom.EmittableContent;
import se.l4.dust.core.internal.template.dom.Empty;
import se.l4.dust.core.internal.template.dom.ExpressionWithContext;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

/**
 * Implementation of {@link TemplateBuilder}.
 * 
 * @author Andreas Holstenson
 *
 */
public class TemplateBuilderImpl
	implements TemplateBuilder
{
	private final Templates templates;
	private final Namespaces namespaceManager;
	
	private final Map<String, String> boundNamespaces;
	private final Expressions expressions;
	
	private final LinkedList<Map<String, AttributeImpl>> mixinAttributes;
	
	private final Map<String, Object> values;
	
	private final Integer id;
	private final LinkedList<Element> stack;
	private final TypeConverter converter;
	
	private ErrorCollector errorCollector;
	private Class<?> context;
	
	private DocType docType;
	private Element root;
	private ResourceLocation source;
	private int line;
	private int column;

	@Inject
	public TemplateBuilderImpl( 
			Namespaces namespaceManager, 
			Templates templates,
			Expressions expressions,
			TypeConverter converter)
	{
		this.namespaceManager = namespaceManager;
		this.templates = templates;
		this.expressions = expressions;
		this.converter = converter;
		this.id = templates.fetchTemplateId();
		
		values = new HashMap<String, Object>();
		boundNamespaces = new HashMap<String, String>();
		mixinAttributes = new LinkedList<Map<String, AttributeImpl>>();
		
		stack = new LinkedList<>();
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

	public void setContext(ResourceLocation source, Class<?> context)
	{
		this.source = source;
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
		if(stack.isEmpty())
		{
			throw raiseError("No current element");
		}
		
		return stack.getLast();
	}
	
	private Element currentN()
	{
		return stack.getLast();
	}
	
	private void setCurrent(Element e)
	{
		stack.add(e);
	}
	
	private void goToParent()
	{
		stack.removeLast();
	}
	
	private boolean isFragment()
	{
		return currentN() instanceof FragmentElement;
	}
	
	@Override
	public TemplateBuilder startElement(String name, String... attributes)
	{
		if(attributes.length % 2 != 0)
		{
			throw new IllegalArgumentException("Attributes must be given as key, value (in pairs)");
		}
		
		// Add mixin attributes to the stack
		mixinAttributes.add(new LinkedHashMap<String, AttributeImpl>());
		
		Element e = new HtmlElement(name);
		applyDebugHints(e);
		
		for(int i=0, n=attributes.length; i<n; i+=2)
		{
			e.addAttribute(new AttributeImpl(attributes[i], Values.of(attributes[i+1])));
		}
		
		if(stack.isEmpty())
		{
			root = e;
		}
		else
		{
			current().addContent(e);
		}
		
		setCurrent(e);
		
		return this;
	}
	
	@Override
	public TemplateBuilder endElement()
	{
		Element current = current();
		
		if(isFragment())
		{
			throw raiseError("Currently building a fragment: " + current);
		}
		
		applyMixins();
		
		mixinAttributes.removeLast();
		
		goToParent();
		
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
		LinkedHashMap<String, AttributeImpl> attributes = new LinkedHashMap<>();
		mixinAttributes.add(attributes);
				
		Element emittable = new FragmentElement(fragment, hasCurrent() ? current() : null, converter, attributes);
		
		applyDebugHints(emittable);
		
		if(stack.isEmpty())
		{
			// No current item, set as root
			root = emittable;
		}
		
		setCurrent(emittable);
		
		return this;
	}
	
	@Override
	public TemplateBuilder endFragment()
	{
		Element current = current();
		if(! isFragment())
		{
			throw raiseError("Currently building an element: " + current);
		}
		
		FragmentElement self = (FragmentElement) current;
		
		goToParent();
		
		self.apply(this);
		
		if(self.wasReplaced())
		{
			applyMixins();
			
			mixinAttributes.removeLast();

			goToParent();
		}
		else
		{
			mixinAttributes.removeLast();
		}
		
		return this;
	}
	
	@Override
	public TemplateBuilder endCurrent()
	{
		current();
		
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
		setAttribute(name, Values.of(value));
		
		return this;
	}
	
	@Override
	public TemplateBuilder setAttribute(String name, Value<?>... value)
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
						new AttributeImpl(name, value)
					);
				
				return this;
			}
		}
		
		Attribute<?> attr = new AttributeImpl(name, value);
		if(current.getClass() == HtmlElement.class)
		{
			attr = attr.bindVia(converter, String.class);
		}
		current.addAttribute(attr);
		
		return this;
	}
	
	@Override
	public TemplateBuilder setAttribute(String name, List<Value<?>> value)
	{
		setAttribute(name, value.toArray(new Value[value.size()]));
		
		return this;
	}
	
	@Override
	public TemplateBuilder addContent(Iterable<? extends Emittable> content)
	{
		current().addContent(content);
		
		return this;
	}
	
	@Override
	public Value<?> createDynamicContent(String expression)
	{
		return expressions.compile(source, boundNamespaces, expression, context);
	}
	
	@Override
	public TemplateBuilder comment(Iterable<? extends Emittable> content)
	{
		Comment comment = new Comment();
		comment.addContent(content);
		
		applyDebugHints(comment);
		
		current().addContent(comment);
		
		return this;
	}
	
	@Override
	public TemplateBuilder comment(String comment)
	{
		return comment(Collections.<Emittable>singletonList(new Text(comment)));
	}
	
	private <T extends Content> T applyDebugHints(T object)
	{
		object.withDebugInfo(source, line, column);
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
		return new ParsedTemplate(source, context.getSimpleName(), docType, root, id);
	}
	
	@Override
	public boolean hasCurrent()
	{
		return ! stack.isEmpty();
	}
	
	private void applyMixins()
	{
		Map<String, AttributeImpl> attrs = mixinAttributes.getLast();
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
	@SuppressWarnings("unchecked")
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
		public AttributeImpl getAttribute(String namespace, String name)
		{
			return mixinAttributes.getLast().get(namespace + "|" + name);
		}
		
		@Override
		public <T> Attribute<T> getAttribute(String namespace, String name, Class<T> type)
		{
			return bindAttribute(getAttribute(namespace, name), type);
		}

		@Override
		public Attribute<?> getAttribute(String name)
		{
			return current().getAttribute(name);
		}
		
		@Override
		public <T> Attribute<T> getAttribute(String name, Class<T> type)
		{
			return bindAttribute(getAttribute(name), type);
		}

		@Override
		public void wrap(ElementWrapper wrapper)
		{
			Element current = current();
			WrappedElement wrapped = new WrappedElement(current, wrapper);
			if(stack.size() == 1)
			{
				// Special case, wrapping the root element
				Empty empty = new Empty();
				empty.addContent(wrapped);
				root = current = empty;
			}
			else
			{
				Element parent = stack.get(stack.size() - 2);
				parent.replaceContent(current, wrapped);
			}
		}
		
		@Override
		public void append(Emittable... content)
		{
			current().addContent(Arrays.asList(content));
		}
		
		@Override
		public void append(List<? extends Emittable> content)
		{
			current().addContent(content);
		}
		
		@Override
		public void prepend(Emittable... content)
		{
			current().prependContent(Arrays.asList(content));
		}
		
		@Override
		public void prepend(List<? extends Emittable> content)
		{
			current().prependContent(content);
		}
		
		@Override
		public MixinEncounter bindNamespace(String prefix, String uri)
		{
			boundNamespaces.put(prefix, uri);
			return this;
		}
		
		@Override
		public Value<?> parseExpression(String expression)
		{
			return expressions.compile(source, boundNamespaces, expression, context);
		}
		
		@Override
		public Value<?> parseExpression(String expression, Object context)
		{
			Expression expr = expressions.compile(source, boundNamespaces, expression, expressions.resolveType(context));
			return new ExpressionWithContext(expr, context);
		}
		
		@Override
		public void setAttribute(String attribute, Value<?> content)
		{
			TemplateBuilderImpl.this.setAttribute(attribute, content);
		}
		
		@Override
		public void error(String error)
		{
			errorCollector.newError(line, column, error);
		}
	}
	
	private <T> Attribute<T> bindAttribute(Attribute<?> attr, Class<T> type)
	{
		if(attr == null)
		{
			return null;
		}
		
		return attr.bindVia(converter, type);
	}
	
	private static class FragmentElement
		extends Element
	{
		private final TemplateFragment fragment;
		private final Element parent;
		private final Map<String, AttributeImpl> attributes;
		private final TypeConverter converter;
		
		private boolean replaced;

		public FragmentElement(TemplateFragment fragment,
				Element parent,
				TypeConverter converter,
				Map<String, AttributeImpl> attributes)
		{
			super("internal:fragment:" + fragment.getClass().getSimpleName());
			
			this.fragment = fragment;
			this.parent = parent;
			this.converter = converter;
			this.attributes = attributes;
		}
		
		@Override
		public void emit(TemplateEmitter emitter, TemplateOutputStream output)
			throws IOException
		{
			throw new UnsupportedOperationException();
		}

		public void apply(final TemplateBuilderImpl builder)
		{
			final Element self = this;
			fragment.build(new FragmentEncounter()
			{
				@Override
				public Attribute<?>[] getAttributes()
				{
					return self.getAttributes();
				}
				
				@Override
				public Attribute<?>[] getAttributesExcluding(String... names)
				{
					List<Attribute<?>> attributes = Lists.newArrayList();
					_outer:
					for(Attribute<?> a : self.getAttributes())
					{
						for(String n : names)
						{
							if(n.equals(a.getName())) continue _outer;
						}
						
						attributes.add(a);
					}
					return attributes.toArray(new Attribute[attributes.size()]);
				}
				
				@Override
				public Attribute<?>[] getAttributesExcluding(Set<String> names)
				{
					List<Attribute<?>> attributes = Lists.newArrayList();
					for(Attribute<?> a : self.getAttributes())
					{
						if(names.contains(a.getName())) continue;
						attributes.add(a);
					}
					return attributes.toArray(new Attribute[attributes.size()]);
				}
				
				private <T> Attribute<T> bindAttribute(Attribute<?> attr, Class<T> type)
				{
					if(attr == null)
					{
						return null;
					}
					
					return attr.bindVia(converter, type);
				}
				
				@Override
				public Attribute<?> getAttribute(String namespace, String name)
				{
					return attributes.get(namespace + '|' + name);
				}
				
				@Override
				public <T> Attribute<T> getAttribute(String namespace, String name, Class<T> type)
				{
					return bindAttribute(getAttribute(namespace, name), type);
				}

				@Override
				public Attribute<?> getAttribute(String name)
				{
					return self.getAttribute(name);
				}
				
				@Override
				public <T> Attribute<T> getAttribute(String name, Class<T> type)
				{
					return bindAttribute(getAttribute(name), type);
				}
				
				@Override
				public Attribute<?> getAttribute(String name, boolean required)
				{
					Attribute<?> attribute = self.getAttribute(name);
					if(attribute == null)
					{
						raiseError("The attribute " + name + " is required but was not found");
					}
					return attribute;
				}
				
				@Override
				public <T> Attribute<T> getAttribute(String name, Class<T> type, boolean required)
				{
					return bindAttribute(getAttribute(name, required), type);
				}
				
				@Override
				public Emittable findParameter(String name)
				{
					return self.getParameter(name);
				}
				
				@Override
				public void addParameter(String name, Emittable content)
				{
					if(parent != null)
					{
						parent.addParameter(name, content);
					}
				}
				
				@Override
				public void raiseError(String message)
				{
					throw builder.raiseError(message);
				}
				
				@Override
				public Emittable[] getBody()
				{
					return getRawContents();
				}
				
				@Override
				public Emittable getScopedBody()
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
					
					if(builder.hasCurrent())
					{
						builder.current().addContent(content);
					}
					else
					{
						builder.root = content;
					}
					
					builder.setCurrent(content);
					
					replaced = true;
				}
				
				@Override
				public void replaceWith(Emittable[] content)
				{
					Element root;
					if(builder.hasCurrent())
					{
						root = builder.current();
					}
					else
					{
						root = builder.root = new Empty();
					}
					
					for(Emittable c : content)
					{
						root.addContent(c);
					}
					
					builder.setCurrent(root);
					
					replaced = true;
				}
				
				@Override
				public void replaceWith(final Iterable<? extends Emittable> content)
				{
					Element root;
					if(builder.hasCurrent())
					{
						root = builder.current();
					}
					else
					{
						root = builder.root = new Empty();
					}
					
					for(Emittable c : content)
					{
						root.addContent(c);
					}
					
					builder.setCurrent(root);
					
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
