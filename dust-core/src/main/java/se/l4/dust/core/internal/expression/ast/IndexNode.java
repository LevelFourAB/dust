package se.l4.dust.core.internal.expression.ast;

import java.util.Arrays;

/**
 * Node representing indexed access.
 * 
 * @author Andreas Holstenson
 *
 */
public class IndexNode
	extends AbstractNode
{
	private final Node left;
	private final Node[] indexes;

	public IndexNode(int line, int position, Node left, Node[] indexes)
	{
		super(line, position);
		this.left = left;
		this.indexes = indexes;
	}
	
	public Node getLeft()
	{
		return left;
	}
	
	public Node[] getIndexes()
	{
		return indexes;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(indexes);
		result = prime * result + ((left == null) ? 0 : left.hashCode());
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
		IndexNode other = (IndexNode) obj;
		if(!Arrays.equals(indexes, other.indexes))
			return false;
		if(left == null)
		{
			if(other.left != null)
				return false;
		}
		else if(!left.equals(other.left))
			return false;
		return true;
	}
	
	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "[left=" + left + ", indexes=" + Arrays.toString(indexes) + "]";
	}
}
