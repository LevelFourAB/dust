package se.l4.dust.api.template.dom;

import java.util.Collection;

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
	
	private Element parent;
	private Content[] contents;

	public Comment()
	{
		contents = EMTPY_CONTENT;
	}
	
	@Override
	public Content doCopy()
	{
		return new Comment();
	}
	
	@Override
	public Content deepCopy()
	{
		Content result = copy();
		Content[] copyContent = new Content[contents.length];
		for(int i=0, n=copyContent.length; i<n; i++)
		{
			copyContent[i] = contents[i].deepCopy();
		}
		
		((Comment) result).contents = copyContent;
		
		return result;
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
