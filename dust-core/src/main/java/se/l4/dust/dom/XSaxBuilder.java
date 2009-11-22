package se.l4.dust.dom;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;

import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.xml.sax.InputSource;

/**
 * Custom version of {@link SAXBuilder} that creates an {@link Document} 
 * instead.
 * 
 * @author Andreas Holstenson
 *
 */
public class XSaxBuilder
	extends SAXBuilder
{
	public XSaxBuilder()
	{
		super();
		
		setFactory(new XJDOMFactory());
	}

	public XSaxBuilder(boolean validate)
	{
		super(validate);
		
		setFactory(new XJDOMFactory());
	}

	public XSaxBuilder(String saxDriverClass, boolean validate)
	{
		super(saxDriverClass, validate);
		
		setFactory(new XJDOMFactory());
	}

	public XSaxBuilder(String saxDriverClass)
	{
		super(saxDriverClass);
		
		setFactory(new XJDOMFactory());
	}
	
	@Override
	public Document build(File file)
		throws JDOMException, IOException
	{
		return (Document) super.build(file);
	}
	
	@Override
	public Document build(InputSource in) 
		throws JDOMException, IOException
	{
		return (Document) super.build(in);
	}
	
	@Override
	public Document build(InputStream in)
		throws JDOMException, IOException
	{
		return (Document) super.build(in);
	}
	
	@Override
	public Document build(InputStream in, String systemId)
		throws JDOMException, IOException
	{
		return (Document) super.build(in, systemId);
	}
	
	@Override
	public Document build(Reader characterStream) 
		throws JDOMException, IOException
	{
		return (Document) super.build(characterStream);
	}

	@Override
	public Document build(Reader characterStream, String systemId)
		throws JDOMException, IOException
	{
		return (Document) super.build(characterStream, systemId);
	}
	
	@Override
	public Document build(String systemId)
		throws JDOMException, IOException
	{
		return (Document) super.build(systemId);
	}
	
	@Override
	public Document build(URL url)
		throws JDOMException, IOException
	{
		return (Document) super.build(url);
	}
}
