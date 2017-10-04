package se.l4.dust.core.internal.expression.ast;

/**
 * Node that changes the sign of a result.
 *
 * @author Andreas Holstenson
 *
 */
public class SignNode
	extends AbstractNode
{
	private final boolean negative;
	private final Node node;

	public SignNode(int line, int position, boolean negative, Node node)
	{
		super(line, position);

		this.negative = negative;
		this.node = node;
	}

	public boolean isNegative()
	{
		return negative;
	}

	public Node getNode()
	{
		return node;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "[negative=" + negative + ", node=" + node + "]";
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (negative ? 1231 : 1237);
		result = prime * result + ((node == null) ? 0 : node.hashCode());
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
		SignNode other = (SignNode) obj;
		if(negative != other.negative)
			return false;
		if(node == null)
		{
			if(other.node != null)
				return false;
		}
		else if(!node.equals(other.node))
			return false;
		return true;
	}
}
