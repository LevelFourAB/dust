package se.l4.dust.api.template.dom;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import se.l4.dust.api.template.Emittable;
import se.l4.dust.api.template.TemplateEmitter;
import se.l4.dust.api.template.TemplateOutputStream;

/**
 * Comment as found in a template.
 *
 * @author Andreas Holstenson
 *
 */
public class Comment
	extends AbstractContent
{
	private static final Content[] EMTPY_CONTENT = new Content[0];

	private Emittable[] contents;

	public Comment()
	{
		contents = EMTPY_CONTENT;
	}

	/**
	 * Add content to this element, see {@link #addContent(Content)}.
	 *
	 * @param objects
	 * @return
	 */
	public void addContent(Iterable<? extends Emittable> objects)
	{
		List<Emittable> result = new ArrayList<>(contents.length + 10);
		for(Emittable e : contents)
		{
			result.add(e);
		}

		for(Emittable e : objects)
		{
			result.add(e);
		}

		contents = result.toArray(new Emittable[result.size()]);
	}

	public Emittable[] getRawContents()
	{
		return contents;
	}

	@Override
	public void emit(TemplateEmitter emitter, TemplateOutputStream output)
		throws IOException
	{
		output.startComment();

		emitter.emit(contents);

		output.endComment();
	}
}
