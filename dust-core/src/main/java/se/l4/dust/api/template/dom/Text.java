package se.l4.dust.api.template.dom;

import java.io.IOException;

import se.l4.dust.api.template.Emittable;
import se.l4.dust.api.template.TemplateEmitter;
import se.l4.dust.api.template.TemplateOutputStream;

/**
 * Text content.
 *
 * @author Andreas Holstenson
 *
 */
public class Text
	implements Emittable
{
	private final String text;

	public Text(String text)
	{
		this.text = text;
	}

	public String getText()
	{
		return text;
	}

	@Override
	public void emit(TemplateEmitter emitter, TemplateOutputStream output)
		throws IOException
	{
		output.text(text);
	}

	@Override
	public String toString()
	{
		return "Text[" + text + "]";
	}
}
