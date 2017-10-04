package se.l4.dust.core.internal.expression.ast;

import java.util.Arrays;

/**
 * Node for an operation that creates an array.
 *
 * @author Andreas Holstenson
 *
 */
public class ArrayNode
	extends AbstractNode
{
	private final Node[] values;

	public ArrayNode(int line, int position, Node[] values)
	{
		super(line, position);

		this.values = values;
	}

	/**
	 * Get the values of this array.
	 *
	 * @return
	 */
	public Node[] getValues()
	{
		return values;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "[values=" + Arrays.toString(values) + "]";
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(values);
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
		ArrayNode other = (ArrayNode) obj;
		if(!Arrays.equals(values, other.values))
			return false;
		return true;
	}
}
