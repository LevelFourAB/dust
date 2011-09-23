package se.l4.dust.core.internal.expression.ast;

/**
 * Node for a ternary if operation.
 * 
 * @author Andreas Holstenson
 *
 */
public class TernaryNode
	extends AbstractNode
{
	private final Node test;
	private final Node left;
	private final Node right;

	public TernaryNode(int line, int position, Node test, Node left, Node right)
	{
		super(line, position);
		
		this.test = test;
		this.left = left;
		this.right = right;
	}
	
	public Node getTest()
	{
		return test;
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
		return getClass().getSimpleName() + "[test=" + test + ", true=" + left + ", false=" + right + "]"; 
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((left == null) ? 0 : left.hashCode());
		result = prime * result + ((right == null) ? 0 : right.hashCode());
		result = prime * result + ((test == null) ? 0 : test.hashCode());
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
		TernaryNode other = (TernaryNode) obj;
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
		if(test == null)
		{
			if(other.test != null)
				return false;
		}
		else if(!test.equals(other.test))
			return false;
		return true;
	}
}
