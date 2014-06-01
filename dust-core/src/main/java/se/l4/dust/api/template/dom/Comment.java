package se.l4.dust.api.template.dom;

import java.io.IOException;
import java.util.Collection;

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
	public Comment addContent(Collection<Emittable> objects)
	{
		Emittable[] result = new Emittable[contents.length + objects.size()];
		System.arraycopy(contents, 0, result, 0, contents.length);
		
		int index = contents.length;
		for(Emittable o : objects)
		{
			result[index++] = o;
		}
		
		contents = result;
		
		return this;
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
