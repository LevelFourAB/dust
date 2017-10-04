package se.l4.dust.core.template.html;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Charsets;

import se.l4.dust.api.template.TemplateOutputStream;
import se.l4.dust.api.template.dom.AttributeImpl;

/**
 * Template output that will output its contents as HTML.
 *
 * @author Andreas Holstenson
 *
 */
public class HtmlTemplateOutput
	implements TemplateOutputStream
{
	protected final Writer writer;
	protected boolean inComment;
	protected boolean written;

	private final List<Boolean> preserveWhitespace;
	protected boolean currentPreserveWhitespace;
	protected boolean lastWhitespace;

	public HtmlTemplateOutput(OutputStream stream)
	{
		this(new OutputStreamWriter(stream, Charsets.UTF_8));
	}

	public HtmlTemplateOutput(Writer writer)
	{
		this.writer = writer;
		preserveWhitespace = new ArrayList<Boolean>(20);
		lastWhitespace = false;
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
			case '"':
				writer.write("&quot;");
				break;
			default:
				writer.write(c);
		}
	}

	private void escapeForce(char c)
		throws IOException
	{
		writer.write("&#");
		writer.write(Integer.toString(c, 10));
		writer.write(';');
	}

	@Override
	public void startElement(String name, String[] attributes)
		throws IOException
	{
		lastWhitespace = false;
		written = true;
		writer.write('<');
		writer.write(name);

		if(attributes.length > 0)
		{
			for(int i=0, n=attributes.length; i<n; i+=2)
			{
				String k = attributes[i];
				String v = attributes[i+1];
				if(k == null)
				{
					break;
				}

				if(v == AttributeImpl.ATTR_EMIT)
				{
					writer.write(' ');
					writer.write(k);
				}
				else if(v != AttributeImpl.ATTR_SKIP)
				{
					writer.write(' ');
					writer.write(k);

					if(v != null)
					{
						writer.write("=\"");
						escape(v);
						writer.write("\"");
					}
				}
			}
		}

		if("textarea".equals(name) || "pre".equals(name))
		{
			currentPreserveWhitespace = Boolean.TRUE;
		}
		preserveWhitespace.add(currentPreserveWhitespace);

		writer.write('>');
	}

	@Override
	public void endElement(String name)
		throws IOException
	{
		lastWhitespace = false;

		writer.write("</");
		writer.write(name);
		writer.write('>');

		preserveWhitespace.remove(preserveWhitespace.size() - 1);
		if(! preserveWhitespace.isEmpty())
		{
			currentPreserveWhitespace = preserveWhitespace.get(preserveWhitespace.size() - 1);
		}
	}

	@Override
	public void element(String name, String[] attributes)
		throws IOException
	{
		lastWhitespace = false;
		written = true;
		writer.write('<');
		writer.write(name);

		if(attributes.length > 0)
		{
			for(int i=0, n=attributes.length; i<n; i+=2)
			{
				String k = attributes[i];
				String v = attributes[i+1];
				if(k == null)
				{
					break;
				}

				if(v == AttributeImpl.ATTR_EMIT)
				{
					writer.write(' ');
					writer.write(k);
				}
				else if(v != AttributeImpl.ATTR_SKIP)
				{
					writer.write(' ');
					writer.write(k);

					if(v != null)
					{
						writer.write("=\"");
						escape(v);
						writer.write("\"");
					}
				}
			}
		}

		writer.write('>');
	}

	@Override
	public void startComment()
		throws IOException
	{
		inComment = true;
		writer.write("<!--");
	}

	@Override
	public void endComment()
		throws IOException
	{
		lastWhitespace = false;
		inComment = false;
		writer.write("-->");
	}

	@Override
	public void text(String text)
		throws IOException
	{
		if(text == null)
		{
			writer.write("null");
			return;
		}

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
		else if(currentPreserveWhitespace)
		{
			for(int i=0, n=text.length(); i<n; i++)
			{
				escape(text.charAt(i));
			}
		}
		else
		{
			for(int i=0, n=text.length(); i<n; i++)
			{
				char c = text.charAt(i);
				boolean whitespace = Character.isWhitespace(c);
				if(whitespace)
				{
					if(lastWhitespace) continue;

					writer.write(' ');
				}
				else
				{
					escape(c);
				}

				lastWhitespace = whitespace;
			}
		}
	}

	@Override
	public void raw(String text)
		throws IOException
	{
		writer.write(text);
	}

	@Override
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

	@Override
	public void close()
		throws IOException
	{
		//writer.flush();
		writer.close();
	}
}
