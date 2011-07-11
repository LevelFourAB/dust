package se.l4.dust.api.template.dom;

/**
 * Text content.
 * 
 * @author Andreas Holstenson
 *
 */
public class Text
	implements Content
{
	private final String text;
	
	private Element parent;
	
	public Text(String text)
	{
		this.text = text;
	}

	public Element getParent()
	{
		return parent;
	}

	public void setParent(Element element)
	{
		this.parent = element;
	}

	public String getText()
	{
		return text;
	}
}
