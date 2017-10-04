package se.l4.dust.api.template.dom;

import se.l4.dust.api.resource.ResourceLocation;

public abstract class AbstractContent
	implements Content
{
	protected ResourceLocation source;
	protected int line;
	protected int column;

	@Override
	public ResourceLocation getDebugSource()
	{
		return source;
	}

	@Override
	public int getLine()
	{
		return line;
	}

	@Override
	public int getColumn()
	{
		return column;
	}

	@Override
	public void withDebugInfo(ResourceLocation source, int line, int column)
	{
		this.source = source;
		this.line = line;
		this.column = column;
	}
}
