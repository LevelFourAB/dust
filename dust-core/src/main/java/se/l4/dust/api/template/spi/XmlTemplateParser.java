package se.l4.dust.api.template.spi;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

import se.l4.dust.Dust;
import se.l4.dust.api.Namespaces;
import se.l4.dust.api.Value;
import se.l4.dust.api.Values;
import se.l4.dust.api.conversion.Conversion;
import se.l4.dust.api.conversion.TypeConverter;
import se.l4.dust.api.resource.Resource;
import se.l4.dust.api.template.Emittable;
import se.l4.dust.api.template.EmittableValue;
import se.l4.dust.api.template.TemplateBuilder;
import se.l4.dust.api.template.TemplateException;
import se.l4.dust.api.template.Templates;
import se.l4.dust.api.template.dom.Text;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.io.Closeables;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Parser for XML based templates.
 * 
 * @author andreas
 *
 */
@Singleton
public class XmlTemplateParser
	implements TemplateParser
{
	private enum Whitespace { KEEP, REMOVE }
	
	private final Templates templates;
	private final Namespaces namespaces;
	private final Function<Value<?>, Emittable> valueToEmittable;

	@Inject
	public XmlTemplateParser(Namespaces namespaces,
			Templates templates,
			final TypeConverter converter)
	{
		this.namespaces = namespaces;
		this.templates = templates;
		
		valueToEmittable = new Function<Value<?>, Emittable>()
		{
			@Override
			public Emittable apply(Value<?> input)
			{
				if(input instanceof Values.StaticValue && input.getType() == String.class)
				{
					return new Text((String) input.get(null, null));
				}
				
				Conversion<?, String> conversion = converter.getDynamicConversion(input.getType(), String.class);
				return new EmittableValue(input, conversion);
			}
		};
	}

	@Override
	public void parse(Resource resource, TemplateBuilder builder)
		throws IOException, TemplateException
	{
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setNamespaceAware(true);
		factory.setValidating(false);
		
		ErrorCollector errors = new ErrorCollector(resource.getLocation());
		builder.withErrorCollector(errors);
		InputStream stream = resource.openStream();
		try
		{
			SAXParser parser = factory.newSAXParser();
		    
			Handler handler = new Handler(namespaces, templates, valueToEmittable, builder, errors);
			parser.setProperty("http://xml.org/sax/properties/lexical-handler", handler);
//			parser.parse(stream, handler);
			
			XMLReader xml = parser.getXMLReader();
			xml.setContentHandler(handler);
			xml.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			xml.setFeature("http://xml.org/sax/features/validation", false);
			
			xml.parse(new InputSource(stream));
			
			if(errors.hasErrors())
			{
				throw errors.raiseException();
			}
		}
		catch(ParserConfigurationException e)
		{
			throw new TemplateException("Unable to parse XML; " + e.getMessage(), e);
		}
		catch(SAXException e)
		{
			errors.newError(0, 0, "Unable to parse template: %s", e);
			throw errors.raiseException();
		}
		finally
		{
			Closeables.closeQuietly(stream);
		}
	}
	
	private static class NsDeclaration
	{
		private final String prefix;
		private final String uri;

		public NsDeclaration(String prefix, String uri)
		{
			this.prefix = prefix;
			this.uri = uri;
		}
	}
	
	private static class Handler
		extends DefaultHandler
		implements LexicalHandler
	{
		private final Namespaces namespaces;
		private final Templates templates;
		private final Function<Value<?>, Emittable> valueToEmittable;
		private final TemplateBuilder builder;
		private final ErrorCollector errors;
		
		private final List<NsDeclaration> declaredNamespaces;
		
		private final List<Whitespace> whitespace;
		private Whitespace currentWhitespace;
		
		private StringBuilder text;
		
		private final ExpressionExtractor extractor;
		
		private Locator locator;
		private int textLine;
		private int textColumn;

		public Handler(Namespaces namespaces, 
				Templates templates, 
				Function<Value<?>, Emittable> valueToEmittable,
				TemplateBuilder builder, 
				ErrorCollector errors)
		{
			this.namespaces = namespaces;
			this.templates = templates;
			this.valueToEmittable = valueToEmittable;
			this.builder = builder;
			this.errors = errors;
			
			declaredNamespaces = Lists.newArrayList();
			text = new StringBuilder();
			
			whitespace = Lists.newArrayList();
			currentWhitespace = Whitespace.KEEP;
			
			extractor = new ExpressionExtractor("${", "}", builder, errors);
		}
		
		@Override
		public void setDocumentLocator(Locator locator)
		{
			this.locator = locator;
		}
		
		@Override
		public void startPrefixMapping(String prefix, String uri)
			throws SAXException
		{
			declaredNamespaces.add(new NsDeclaration(prefix, uri));
			
			builder.bindNamespace(prefix, uri);
		}
		
		@Override
		public void endPrefixMapping(String prefix)
			throws SAXException
		{
			builder.unbindNamespace(prefix);
		}
		
		private void newError(String error, Object... params)
		{
			errors.newError(
				locator.getLineNumber(), 
				locator.getColumnNumber(), 
				error,
				params
			);
		}
		
		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes)
			throws SAXException
		{
			flushCharacters();
			
			builder.addDebugHint(locator.getLineNumber(), locator.getColumnNumber());
			
			int wsIdx = attributes.getIndex(Dust.NAMESPACE_COMMON, "whitespace");
			if(wsIdx >= 0)
			{
				String value = attributes.getValue(wsIdx);
				if("ignore".equals(value))
				{
					currentWhitespace = Whitespace.REMOVE; 
				}
				else if("keep".equals(value))
				{
					currentWhitespace = Whitespace.KEEP;
				}
				else
				{
					errors.newError(
						locator.getLineNumber(), locator.getColumnNumber(),
						"Invalid value for argument `whitespace`: " + value
					);
					currentWhitespace = Whitespace.KEEP;
				}
			}
			
			whitespace.add(currentWhitespace);
			
			if(isParameterNamespace(uri))
			{
				builder.startParameter(localName);
			}
			else if(namespaces.isBound(uri))
			{
				// This namespace is managed by us, treat as component
				Templates.TemplateNamespace tpl = templates.getNamespace(uri);
				if(tpl.hasFragment(localName))
				{
					builder.startFragment(localName, uri);
				}
				else
				{
					builder.startElement(qName);
					newError("The component %s does not exist in the namespace %s", localName, uri);
					return;
				}
			}
			else
			{
				builder.startElement(qName);
			}

			// Setup all namespaces
			bindNamespaces();
			
			// Copy all attributes
			for(int i=0, n=attributes.getLength(); i<n; i++)
			{
				if(i == wsIdx) continue;
				
				String name = attributes.getQName(i);
				String value = attributes.getValue(i);
				
				List<Value<?>> contents = extractor.parse(
					errors.getSource(),
					locator.getLineNumber(), 
					locator.getColumnNumber(), 
					value
				);
				
				builder.addDebugHint(locator.getLineNumber(), locator.getColumnNumber());
				builder.setAttribute(name, contents);
			}
		}
		
		private boolean isParameterNamespace(String uri)
		{
			return Dust.NAMESPACE_PARAMETERS.equals(uri);
		}

		@Override
		public void endElement(String uri, String localName, String qName)
			throws SAXException
		{
			flushCharacters();
		
			// Reset whitespace
			whitespace.remove(whitespace.size() - 1);
			if(! whitespace.isEmpty())
			{
				currentWhitespace = whitespace.get(whitespace.size() - 1);
			}
			
			builder.addDebugHint(locator.getLineNumber(), locator.getColumnNumber());
			builder.endCurrent();
		}
		
		@Override
		public void ignorableWhitespace(char[] ch, int start, int length)
			throws SAXException
		{
			characters(ch, start, length);
		}
		
		@Override
		public void characters(char[] ch, int start, int length)
			throws SAXException
		{
//			if(previousCDATA != inCDATA)
//			{
//				flushCharacters();
//			}

			if(text.length() == 0)
			{
				// Started text, save offsets
				textLine = locator.getLineNumber();
				textColumn = locator.getColumnNumber();
			}
			
	        text.append(ch, start, length);
		}
		
		protected void flushCharacters()
		{
			if(text.length() == 0) return;
			
			String text = this.text.toString();
			this.text.setLength(0);
			
			if(currentWhitespace == Whitespace.REMOVE && text.trim().isEmpty())
			{
				return;
			}
			
			if(false == builder.hasCurrent())
			{
				// TODO: What should we do?
			}
			else
			{
				List<Value<?>> content = extractor.parse(errors.getSource(), textLine, textColumn, text);
				if(currentWhitespace == Whitespace.REMOVE)
				{
					Iterator<Value<?>> it = content.iterator();
					while(it.hasNext())
					{
						Value<?> c = it.next();
						if(c instanceof Values.StaticValue && ((String) c.get(null, null)).trim().isEmpty())
						{
							it.remove();
						}
					}
				}
				
				builder.addContent(toEmittable(content));
			}
		}
		
		@Override
		public void comment(char[] ch, int start, int length)
			throws SAXException
		{
			flushCharacters();

			// Check if this is an internal comment
			if(length >= 1 && ch[0] == '#') return;
			
			// Otherwise parse the text
			String commentText = new String(ch, start, length);
			List<Value<?>> content = extractor.parse(
				errors.getSource(),
				locator.getLineNumber(), 
				locator.getColumnNumber(), 
				commentText
			);
			
			builder.comment(toEmittable(content));
		}
		
		private Iterable<Emittable> toEmittable(Iterable<Value<?>> values)
		{
			return Iterables.transform(values, valueToEmittable);
		}
		
		@Override
		public void startDTD(String name, String publicId, String systemId)
			throws SAXException
		{
			builder.setDoctype(name, publicId, systemId);
		}
		
		@Override
		public void endDTD()
			throws SAXException
		{
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void startEntity(String name)
			throws SAXException
		{
		}
		
		@Override
		public void endEntity(String name)
			throws SAXException
		{
		}
		
		@Override
		public void startCDATA()
			throws SAXException
		{
		}
		
		@Override
		public void endCDATA()
			throws SAXException
		{
		}

		private void bindNamespaces()
		{
			for(int i=0, n=declaredNamespaces.size(); i<n; i++)
			{
				NsDeclaration declaration = declaredNamespaces.get(i);
				if(namespaces.isBound(declaration.uri))
				{
					// Don't show bound namespaces
					continue;
				}
				
				builder.setAttribute("xmlns:" + declaration.prefix, declaration.uri);
			}
			
			declaredNamespaces.clear();
		}
	}
}
