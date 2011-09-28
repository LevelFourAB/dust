package se.l4.dust.core.template.html;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Set;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;

import se.l4.dust.api.template.dom.Element.Attribute;
import se.l4.dust.api.template.spi.TemplateOutputStream;

/**
 * Template output that will output its contents as HTML.
 * 
 * @author Andreas Holstenson
 *
 */
public class HtmlTemplateOutput
	implements TemplateOutputStream
{
	private static final Set<String> singleTags;
	
	static
	{
		singleTags = ImmutableSet.<String>builder()
			.add("br")
			.add("hr")
			.add("img")
			.build();
	}
	
	private final OutputStreamWriter writer;
	private boolean inComment;
	private boolean written;

	public HtmlTemplateOutput(OutputStream stream)
	{
		writer = new OutputStreamWriter(stream, Charsets.UTF_8);
	}
	
	private void escape(String in)
		throws IOException
	{
		for(int i=0, n=in.length(); i<n; i++)
		{
			escape(in.charAt(i));
		}
	}

	private void escape(char c)
		throws IOException
	{
		// TODO: Escaping of named characters
		switch(c)
		{
			case '<':
				writer.write("&lt;");
				break;
			case '>':
				writer.write("&gt;");
				break;
			case '&':
				writer.write("&amp;");
				break;
			default:
				if(c > 0x7F)
				{
					escapeForce(c);
				}
				else
				{
					writer.write(c);
				}
		}
	}

	private void escapeForce(char c)
		throws IOException
	{
		writer.write("&#");
		writer.write(Integer.toString(c, 10));
		writer.write(';');
	}

	public void startElement(String name, String[] attributes, boolean close)
		throws IOException
	{
		written = true;
		writer.write('<');
		writer.write(name);
		
		if(attributes.length > 0)
		{
			for(int i=0, n=attributes.length; i<n; i+=2)
			{
				String v = attributes[i+1];
				if(v == Attribute.ATTR_EMIT)
				{
					writer.write(' ');
					writer.write(attributes[i]);
				}
				else if(v != Attribute.ATTR_SKIP)
				{
					writer.write(' ');
					writer.write(attributes[i]);
					
					if(v != null)
					{
						writer.write("=\"");
						escape(attributes[i+1]);
						writer.write("\"");
					}
				}
			}
		}
		
		if(close) writer.write('/');
		
		writer.write('>');
	}

	public void endElement(String name)
		throws IOException
	{
		if(singleTags.contains(name))
		{
			return;
		}
		
		writer.write("</");
		writer.write(name);
		writer.write('>');
	}

	public void startComment()
		throws IOException
	{
		inComment = true;
		writer.write("<!--");
	}

	public void endComment()
		throws IOException
	{
		inComment = false;
		writer.write("-->");
	}

	public void text(String text)
		throws IOException
	{
		if(inComment)
		{
			for(int i=0, n=text.length(); i<n; i++)
			{
				char c = text.charAt(i);
				
				if(c == '-' && i < n - 1 && text.charAt(i+1) == '-')
				{
					// Escape this character
					escapeForce(c);
				}
				else
				{
					writer.write(text.charAt(i));
				}
			}
		}
		else
		{
			boolean lastWhitespace = false;
			
			for(int i=0, n=text.length(); i<n; i++)
			{
				char c = text.charAt(i);
				boolean whitespace = Character.isWhitespace(c);
				if(lastWhitespace && whitespace) continue;
				
				escape(text.charAt(i));
				
				lastWhitespace = whitespace;
			}
		}
	}
	
	public void raw(String text)
		throws IOException
	{
		writer.write(text);
	}
	
	public void docType(String name, String publicId, String systemId)
		throws IOException
	{
		// Do not output if illegal
		if(written) return;
		
		writer.write("<!DOCTYPE ");
		writer.write(name);
		
		if(publicId != null)
		{
			writer.write(" PUBLIC \"");
			writer.write(publicId);
			writer.write('"');
		}
		
		if(systemId != null)
		{
			if(publicId == null) writer.write(" SYSTEM");
			writer.write(" \"");
			writer.write(systemId);
			writer.write('"');
		}
		
		writer.write('>');
		
		written = true;
	}

	public void close()
		throws IOException
	{
		writer.flush();
		writer.close();
	}
}
