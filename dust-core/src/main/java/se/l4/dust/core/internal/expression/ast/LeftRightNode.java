package se.l4.dust.core.internal.expression.ast;

/**
 * Abstract implementation of a node that has a left and a right part.
 *
 * @author Andreas Holstenson
 *
 */
public abstract class LeftRightNode
	extends AbstractNode
{
	protected final Node left;
	protected final Node right;

	public LeftRightNode(int line, int position, Node left, Node right)
	{
		super(line, position);

		this.left = left;
		this.right = right;
	}

	public Node getLeft()
	{
		return left;
	}

	public Node getRight()
	{
		return right;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "[left=" + left + ", right=" + right + "]";
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((left == null) ? 0 : left.hashCode());
		result = prime * result + ((right == null) ? 0 : right.hashCode());
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
		LeftRightNode other = (LeftRightNode) obj;
		if(left == null)
		{
			if(other.left != null)
				return false;
		}
		else if(!left.equals(other.left))
			return false;
		if(right == null)
		{
			if(other.right != null)
				return false;
		}
		else if(!right.equals(other.right))
			return false;
		return true;
	}
}
