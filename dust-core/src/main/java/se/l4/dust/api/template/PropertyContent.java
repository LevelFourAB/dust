package se.l4.dust.api.template;

import org.jdom.Content;

public abstract class PropertyContent
	extends Content
{
	
	@Override
	public String getValue()
	{
		return null;
	}
	
	public abstract Object getValue(Object root);
	
	public abstract void setValue(Object root, Object data);
}
