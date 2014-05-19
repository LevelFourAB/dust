package se.l4.dust.core.internal.template.dom;

import se.l4.dust.api.template.dom.Content;
import se.l4.dust.api.template.dom.Element;

public class Empty
	extends Element
{
	public Empty()
	{
		super("");
	}
	
	@Override
	public Content doCopy()
	{
		return new Empty();
	}
}
