package se.l4.dust.core.internal.template.dom;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import org.jdom.Attribute;
import org.jdom.CDATA;
import org.jdom.Comment;
import org.jdom.Content;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.Text;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import se.l4.dust.core.internal.template.components.RawComponent;

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
	
	/** Copied from XMLOutputter */
	private void printNamespace(Writer out, Namespace ns,
			NamespaceStack namespaces) throws IOException
	{
		String prefix = ns.getPrefix();
		String uri = ns.getURI();

		// Already printed namespace decl?
		if(uri.equals(namespaces.getURI(prefix)))
		{
			return;
		}

		out.write(" xmlns");
		if(!prefix.equals(""))
		{
			out.write(":");
			out.write(prefix);
		}
		out.write("=\"");
		out.write(escapeAttributeEntities(uri));
		out.write("\"");
		namespaces.push(ns);
	}
	
	/** Copied from XMLOutputter */
	private void printQualifiedName(Writer out, Attribute a) throws IOException {
        String prefix = a.getNamespace().getPrefix();
        if ((prefix != null) && (!prefix.equals(""))) {
            out.write(prefix);
            out.write(':');
            out.write(a.getName());
        }
        else {
            out.write(a.getName());
        }
    }
	
	@Override
	protected void printAttributes(Writer out, List attributes, Element parent,
			NamespaceStack namespaces)
		throws IOException
	{
		List<Attribute> attrs = (List<Attribute>) attributes;
		for(Attribute a : attrs)
		{
			Namespace ns = a.getNamespace();
			
			if(ns == Namespace.XML_NAMESPACE && "space".equals(a.getName()))
			{
				continue;
			}
			
			if((ns != Namespace.NO_NAMESPACE)
					&& (ns != Namespace.XML_NAMESPACE))
			{
				printNamespace(out, ns, namespaces);
			}

			out.write(" ");
			printQualifiedName(out, a);
			out.write("=");

			out.write("\"");
			out.write(escapeAttributeEntities(a.getValue()));
			out.write("\"");
		}
	}

	@Override
	protected void printElement(Writer out, Element element, int level, NamespaceStack namespaces)
		throws IOException
	{
		if(element instanceof RawComponent)
		{
			out.write(element.getAttributeValue("value"));
			
			return;
		}
		else if(element.getNamespace() == Namespace.NO_NAMESPACE)
		{
			String name = element.getName();
			if("script".equals(name))
			{
				Format previousFormat = currentFormat;
				
				currentFormat = preserveFormat;
				
				out.write("<script");
				
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
					else if(c instanceof Comment)
					{
						printComment(out, (Comment) c);
					}
					else if(c instanceof Element)
					{
						printElement(out, (Element) c, level, namespaces);
					}
				}
				
				out.write("</script>");
				
				currentFormat = previousFormat;
				
				return;
			}
			else if("pre".equals(element.getName()))
			{
				Format previousFormat = currentFormat;
				currentFormat = preserveFormat;
				
				super.printElement(out, element, level, namespaces);
				
				currentFormat = previousFormat;
				
				return;
			}
			
			if(currentFormat.getExpandEmptyElements())
			{
				// HTML output
				if("br".equals(name) || "hr".equals(name))
				{
					out.write("<");
					out.write(name);
						
					printAttributes(out, element.getAttributes(), element, namespaces);
						
					out.write(">");
					
					return;
				}
			}
		}

		super.printElement(out, element, level, namespaces);
	}
}
