package se.l4.dust.core.internal.expression.ast;

/**
 * Node that represents a static keyword such as {@code true} of {@code false}.
 * 
 * @author Andreas Holstenson
 *
 */
public class KeywordNode
	extends AbstractNode
{
	private final Type type;

	public enum Type
	{
		TRUE,
		FALSE,
		NULL,
		THIS
	}
	
	public KeywordNode(int line, int position, Type type)
	{
		super(line, position);
		
		this.type = type;
	}
	
	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "[" + type + "]";
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj)
			return true;
		if(obj == null)
			return false;
		if(getClass() != obj.getClass())
			return false;
		KeywordNode other = (KeywordNode) obj;
		if(type != other.type)
			return false;
		return true;
	}
}
