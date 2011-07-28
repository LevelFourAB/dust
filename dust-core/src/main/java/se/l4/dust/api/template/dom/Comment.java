package se.l4.dust.api.template.dom;

import java.util.Collection;

/**
 * Comment as found in a template.
 * 
 * @author Andreas Holstenson
 *
 */
public class Comment
	implements Content
{
	private static final Content[] EMTPY_CONTENT = new Content[0];
	
	private Element parent;
	private Content[] contents;
	
	public Comment()
	{
		contents = EMTPY_CONTENT;
	}
	
	public Content copy()
	{
		return new Comment();
	}

	public Element getParent()
	{
		return parent;
	}

	public void setParent(Element element)
	{
		this.parent = element;
	}

	/**
	 * Add content to this element, see {@link #addContent(Content)}.
	 * 
	 * @param objects
	 * @return
	 */
	public Comment addContent(Collection<Content> objects)
	{
		Content[] result = new Content[contents.length + objects.size()];
		System.arraycopy(contents, 0, result, 0, contents.length);
		
		int index = contents.length;
		for(Content o : objects)
		{
			result[index++] = o;
		}
		
		contents = result;
		
		return this;
	}

	public Content[] getRawContents()
	{
		return contents;
	}
	
}
