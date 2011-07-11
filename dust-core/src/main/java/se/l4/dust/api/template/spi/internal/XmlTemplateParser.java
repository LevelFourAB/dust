package se.l4.dust.api.template.spi.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

import com.google.inject.Inject;

import se.l4.dust.api.NamespaceManager;
import se.l4.dust.api.TemplateException;
import se.l4.dust.api.TemplateManager;
import se.l4.dust.api.template.dom.Content;
import se.l4.dust.api.template.spi.ErrorCollector;
import se.l4.dust.api.template.spi.ExpressionExtractor;
import se.l4.dust.api.template.spi.TemplateBuilder;
import se.l4.dust.api.template.spi.TemplateParser;

public class XmlTemplateParser
	implements TemplateParser
{
	private final TemplateManager templates;
	private final NamespaceManager namespaces;

	@Inject
	public XmlTemplateParser(NamespaceManager namespaces, TemplateManager templates)
	{
		this.namespaces = namespaces;
		this.templates = templates;
	}

	public void parse(InputStream stream, String name, TemplateBuilder builder)
		throws IOException, TemplateException
	{
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setNamespaceAware(true);
		
		ErrorCollector errors = new ErrorCollector(name);
		try
		{
			SAXParser parser = factory.newSAXParser();
			Handler handler = new Handler(namespaces, templates, builder, errors);
			parser.setProperty("http://xml.org/sax/properties/lexical-handler", handler);
			parser.parse(stream, handler);
			
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
		private final NamespaceManager namespaces;
		private final TemplateManager templates;
		private final TemplateBuilder builder;
		private final ErrorCollector errors;
		
		private final List<NsDeclaration> declaredNamespaces;
		
		private StringBuilder text;
		
		private final ExpressionExtractor extractor;
		
		private Locator locator;
		private int textLine;
		private int textColumn;

		public Handler(NamespaceManager namespaces, 
				TemplateManager templates, 
				TemplateBuilder builder, 
				ErrorCollector errors)
		{
			this.namespaces = namespaces;
			this.templates = templates;
			this.builder = builder;
			this.errors = errors;
			
			declaredNamespaces = new ArrayList<NsDeclaration>();
			text = new StringBuilder();
			
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
			
			if(namespaces.isBound(uri))
			{
				// This namespace is managed by us, treat as component
				
				if(templates.hasComponent(uri, localName))
				{
					Class<?> component = templates.getComponent(uri, localName);
					
					builder.startComponent(component);
				}
				else
				{
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
			
			// Setup all attributes
			for(int i=0, n=attributes.getLength(); i<n; i++)
			{
				String name = attributes.getQName(i);
				String value = attributes.getValue(i);
				
				List<Content> contents = extractor.parse(
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
			
			if(false == builder.hasCurrent())
			{
				// TODO: What should we do?
			}
			else
			{
				List<Content> content = extractor.parse(textLine, textColumn, text);
				builder.addContent(content);
			}
			
			text.setLength(0);
		}
		
		public void comment(char[] ch, int start, int length)
			throws SAXException
		{
			flushCharacters();

			String commentText = new String(ch, start, length);
			List<Content> content = extractor.parse(
				locator.getLineNumber(), 
				locator.getColumnNumber(), 
				commentText
			);
			
			builder.comment(content);
		}
		
		public void startDTD(String name, String publicId, String systemId)
			throws SAXException
		{
			builder.setDoctype(name, publicId, systemId);
		}
		
		public void endDTD()
			throws SAXException
		{
			// TODO Auto-generated method stub
			
		}
		
		public void startEntity(String name)
			throws SAXException
		{
		}
		
		public void endEntity(String name)
			throws SAXException
		{
		}
		
		public void startCDATA()
			throws SAXException
		{
		}
		
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
