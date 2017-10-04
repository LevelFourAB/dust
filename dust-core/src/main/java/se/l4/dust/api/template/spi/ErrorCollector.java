package se.l4.dust.api.template.spi;

import java.util.ArrayList;
import java.util.List;

import se.l4.dust.api.resource.ResourceLocation;
import se.l4.dust.api.template.TemplateException;

/**
 * Collector for template errors. This can be used during template parsing
 * to create a combined list of errors found in the current template.
 *
 * @author Andreas Holstenson
 *
 */
public class ErrorCollector
{
	private final List<String> errors;
	private final ResourceLocation source;

	public ErrorCollector(ResourceLocation source)
	{
		this.source = source;
		errors = new ArrayList<String>();
	}

	public ResourceLocation getSource()
	{
		return source;
	}

	public void newError(int line, int column, String error, Object... params)
	{
		errors.add("Error at line " + line + ", column " + column + ": " + format(error, params));
	}

	private String format(String message, Object... params)
	{
		for(int i=0, n=params.length; i<n; i++)
		{
			if(params[i] instanceof Throwable)
			{
				params[i] = ((Throwable) params[i]).getMessage();
			}
		}

		return String.format(message, params);
	}

	public TemplateException raiseException()
	{
		StringBuilder builder = new StringBuilder();
		builder.append(source + ":\n The template could not be processed:");
		for(String error : errors)
		{
			builder
				.append("\n\n  ")
				.append(error);
		}

		return new TemplateException(builder.toString());
	}

	public boolean hasErrors()
	{
		return false == errors.isEmpty();
	}
}
