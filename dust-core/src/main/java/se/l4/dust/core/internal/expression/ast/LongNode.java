package se.l4.dust.core.internal.expression.ast;

/**
 * Node holding a {@code long} value.
 *
 * @author Andreas Holstenson
 *
 */
public class LongNode
	extends AbstractNode
{
	private final long value;

	public LongNode(int line, int position, long value)
	{
		super(line, position);

		this.value = value;
	}

	public long getValue()
	{
		return value;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "[" + value + "]";
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (value ^ (value >>> 32));
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
		LongNode other = (LongNode) obj;
		if(value != other.value)
			return false;
		return true;
	}
}
