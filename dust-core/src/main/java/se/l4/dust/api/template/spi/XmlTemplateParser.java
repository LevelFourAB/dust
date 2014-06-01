package se.l4.dust.api.template.spi;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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

import se.l4.dust.api.Namespaces;
import se.l4.dust.api.template.TemplateBuilder;
import se.l4.dust.api.template.TemplateException;
import se.l4.dust.api.template.Templates;
import se.l4.dust.api.template.dom.Content;
import se.l4.dust.api.template.dom.Text;

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
	private final Templates templates;
	private final Namespaces namespaces;

	@Inject
	public XmlTemplateParser(Namespaces namespaces, Templates templates)
	{
		this.namespaces = namespaces;
		this.templates = templates;
	}

	@Override
	public void parse(InputStream stream, String name, TemplateBuilder builder)
		throws IOException, TemplateException
	{
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setNamespaceAware(true);
		factory.setValidating(false);
		
		ErrorCollector errors = new ErrorCollector(name);
		builder.withErrorCollector(errors);
		try
		{
			SAXParser parser = factory.newSAXParser();
		    
			Handler handler = new Handler(namespaces, templates, builder, errors);
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
		private final TemplateBuilder builder;
		private final ErrorCollector errors;
		
		private final List<NsDeclaration> declaredNamespaces;
		
		private final List<Boolean> ignoreWhitespace;
		private Boolean currentIgnoreWhitespace;
		
		private StringBuilder text;
		
		private final ExpressionExtractor extractor;
		
		private Locator locator;
		private int textLine;
		private int textColumn;

		public Handler(Namespaces namespaces, 
				Templates templates, 
				TemplateBuilder builder, 
				ErrorCollector errors)
		{
			this.namespaces = namespaces;
			this.templates = templates;
			this.builder = builder;
			this.errors = errors;
			
			declaredNamespaces = new ArrayList<NsDeclaration>();
			text = new StringBuilder();
			
			ignoreWhitespace = new ArrayList<Boolean>();
			currentIgnoreWhitespace = Boolean.FALSE;
			
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
			
			int wsIdx = attributes.getIndex("dust:common", "whitespace");
			if(wsIdx >= 0)
			{
				String value = attributes.getValue(wsIdx);
				if("ignore".equals(value))
				{
					currentIgnoreWhitespace = Boolean.TRUE; 
				}
				else
				{
					currentIgnoreWhitespace = Boolean.FALSE;
				}
			}
			
			ignoreWhitespace.add(currentIgnoreWhitespace);
			
			if(namespaces.isBound(uri))
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
				
				List<Content> contents = extractor.parse(
					errors.getName(),
					locator.getLineNumber(), 
					locator.getLineNumber(), 
					value
				);
				
				builder.setAttribute(name, contents);
			}
		}
		
		@Override
		public void endElement(String uri, String localName, String qName)
			throws SAXException
		{
			flushCharacters();
		
			// Reset whitespace
			ignoreWhitespace.remove(ignoreWhitespace.size() - 1);
			if(! ignoreWhitespace.isEmpty())
			{
				currentIgnoreWhitespace = ignoreWhitespace.get(ignoreWhitespace.size() - 1);
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
			
			if(currentIgnoreWhitespace == Boolean.TRUE && text.trim().isEmpty())
			{
				return;
			}
			
			if(false == builder.hasCurrent())
			{
				// TODO: What should we do?
			}
			else
			{
				List<Content> content = extractor.parse(errors.getName(), textLine, textColumn, text);
				if(currentIgnoreWhitespace)
				{
					Iterator<Content> it = content.iterator();
					while(it.hasNext())
					{
						Content c = it.next();
						if(c instanceof Text && ((Text) c).getText().trim().isEmpty())
						{
							it.remove();
						}
					}
				}
				
				builder.addContent(content);
			}
		}
		
		@Override
		public void comment(char[] ch, int start, int length)
			throws SAXException
		{
			flushCharacters();

			String commentText = new String(ch, start, length);
			List<Content> content = extractor.parse(
				errors.getName(),
				locator.getLineNumber(), 
				locator.getColumnNumber(), 
				commentText
			);
			
			builder.comment(content);
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
