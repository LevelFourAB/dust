package se.l4.dust.core.internal.template.dom;

import org.jdom.Element;
import org.jdom.input.SAXHandler;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import se.l4.dust.api.ComponentException;
import se.l4.dust.api.TemplateException;

import com.google.inject.Inject;

/**
 * Custom {@link SAXHandler} for template parsing, used so that better error
 * messages (with line numbers and columns) can be reported.
 * 
 * @author Andreas Holstenson
 *
 */
public class TemplateSAXHandler
	extends SAXHandler
{
	@Inject
	public TemplateSAXHandler(TemplateFactory factory)
	{
		super(factory);
	}

	@Override
	public void startElement(String namespaceURI, String localName,
			String qName, Attributes atts)
		throws SAXException
	{
		try
		{
			super.startElement(namespaceURI, localName, qName, atts);
			
			Element e = getCurrentElement();
			Locator l = getDocumentLocator();
			if(e instanceof LocationAware && l != null)
			{
				((LocationAware) e).setLocation(l.getLineNumber(), l.getColumnNumber());
			}
		}
		catch(ComponentException e)
		{
			Locator l = getDocumentLocator();
			if(l != null)
			{
				// We have access to location information so we enhance the error message
				String msg = String.format("Error on line %s, column %s: %s", 
					l.getLineNumber(), l.getColumnNumber(), e.getMessage());
				throw new TemplateException(msg, e);
			}
			else
			{
				throw new TemplateException(e.getMessage(), e);
			}
		}
	}
}
