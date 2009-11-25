package se.l4.dust.core.internal.template.dom;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import org.jdom.CDATA;
import org.jdom.Content;
import org.jdom.Element;
import org.jdom.Text;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class TemplateOutputter
	extends XMLOutputter
{
	public TemplateOutputter()
	{
		super();
	}

	public TemplateOutputter(Format format)
	{
		super(format);
	}

	public TemplateOutputter(XMLOutputter that)
	{
		super(that);
	}

	@Override
	protected void printElement(Writer out, Element element, int level, NamespaceStack namespaces)
		throws IOException
	{
		if("".equals(element.getNamespaceURI()) && "script".equals(element.getName()))
		{
			Format previousFormat = currentFormat;
			
			currentFormat = preserveFormat;
			
			out.write("<script ");
			
			printAttributes(out, element.getAttributes(), element, namespaces);
			
			out.write(">");
			
			for(Content c : (List<Content>) element.getContent())
			{
				if(c instanceof Text)
				{
					printText(out, (Text) c);
				}
				else if(c instanceof CDATA)
				{
					printCDATA(out, (CDATA) c);
				}
			}
			
			out.write("</script>");
			
			currentFormat = previousFormat;
		}
		else
		{
			super.printElement(out, element, level, namespaces);
		}
	}
}
