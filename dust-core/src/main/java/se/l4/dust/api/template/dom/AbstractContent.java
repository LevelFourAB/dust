package se.l4.dust.api.template.dom;

public abstract class AbstractContent
	implements Content
{
	protected String source;
	protected int line;
	protected int column;
	
	@Override
	public String getDebugSource()
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
	public void withDebugInfo(String source, int line, int column)
	{
		this.source = source;
		this.line = line;
		this.column = column;
	}
	
	@Override
	public final Content copy()
	{
		Content copied = doCopy();
		copied.withDebugInfo(source, line, column);
		return copied;
	}
	
	protected abstract Content doCopy();
}
