package se.l4.dust.core.template.html;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Set;

import se.l4.dust.api.template.HTML;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;

/**
 * Template output that will output its contents as HTML.
 * 
 * @author Andreas Holstenson
 *
 */
public class FormattedHtmlTemplateOutput
	extends HtmlTemplateOutput
{
	private static final Set<String> SINGLE_LINE = ImmutableSet.of(
		"h1", "h2", "h3", "h4", "h5", "h6", "title", "link", "meta", "script"
	);
	
	private static final Set<String> EXTRA_BLOCKS = ImmutableSet.of(
		"script", "meta"
	);
	
	private int level;
	private boolean previousNewline;
	
	public FormattedHtmlTemplateOutput(OutputStream stream)
	{
		this(new OutputStreamWriter(stream, Charsets.UTF_8));
	}
	
	public FormattedHtmlTemplateOutput(Writer writer)
	{
		super(writer);
	}
	
	private void writeIndentation(boolean newline)
		throws IOException
	{
		if(!newline) return;
		if(newline)
		{
			writer.write('\n');
		}
		
		for(int i=0; i<level; i++)
		{
			writer.write('\t');
		}
		
		lastWhitespace = true;
	}
	
	@Override
	public void startElement(String name, String[] attributes)
		throws IOException
	{
		boolean block = isBlock(name);
		if(block && written)
		{
			writeIndentation(! previousNewline);
		}
		
		super.startElement(name, attributes);
		
		if(block && ! SINGLE_LINE.contains(name.toLowerCase()))
		{
			level++;
			writeIndentation(true);
			previousNewline = true;
		}
		else
		{
			previousNewline = false;
		}
	}
	
	@Override
	public void endElement(String name)
		throws IOException
	{
		boolean block = isBlock(name);
		if(block && ! SINGLE_LINE.contains(name.toLowerCase()))
		{
			level--;
			writeIndentation(true);
		}
		
		super.endElement(name);
		
		if(block)
		{
			writeIndentation(true);
			previousNewline = true;
		}
		else
		{
			previousNewline = false;
		}
	}
	
	@Override
	public void element(String name, String[] attributes)
		throws IOException
	{
		boolean block = isBlock(name);
		if(block)
		{
			writeIndentation(! previousNewline);
		}
		
		super.element(name, attributes);
		previousNewline = false;
	}
	
	@Override
	public void text(String text)
		throws IOException
	{
		super.text(text);
		
		if(previousNewline)
		{
			for(int i=0, n=text.length(); i<n; i++)
			{
				char c = text.charAt(i);
				if(! Character.isWhitespace(c))
				{
					previousNewline = false;
					break;
				}
			}
		}
	}
	
	@Override
	public void startComment()
		throws IOException
	{
		if(written)
		{
			writeIndentation(! previousNewline);
		}
		
		super.startComment();
		
		previousNewline = true;
	}
	
	@Override
	public void endComment()
		throws IOException
	{
		previousNewline = false;
		super.endComment();
	}
	
	private boolean isBlock(String name)
	{
		return EXTRA_BLOCKS.contains(name.toLowerCase()) || ! HTML.isInline(name);
	}
}
