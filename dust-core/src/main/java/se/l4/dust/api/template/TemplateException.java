package se.l4.dust.api.template;

import se.l4.dust.api.template.dom.Content;

/**
 * Indicate that an error occurred while parsing or processing a template.
 * 
 * @author Andreas Holstenson
 *
 */
public class TemplateException
	extends RuntimeException
{
	private String source;
	private int line;
	private int column;
	
	public TemplateException()
	{
		super();
	}

	public TemplateException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public TemplateException(String message)
	{
		super(message);
	}

	public TemplateException(Throwable cause)
	{
		super(cause);
	}
	
	@Override
	public String getMessage()
	{
		if(this.source != null)
		{
			return source + ":\n  Error on line " + line + ", column " + column + ":\n\n" + super.getMessage();
		}
		
		return super.getMessage();
	}
	
	public TemplateException withDebugInfo(Object o)
	{
		if(o instanceof Content)
		{
			Content c = (Content) o;
			if(c.getDebugSource() != null)
			{
				return withDebugInfo(c.getDebugSource().toString(), c.getLine(), c.getColumn());
			}
		}
		
		return this;
	}
	
	public TemplateException withDebugInfo(String source, int line, int column)
	{
		if(this.source != null) return this;
		
		this.source = source;
		this.line = line;
		this.column = column;
		
		return this;
	}
}
