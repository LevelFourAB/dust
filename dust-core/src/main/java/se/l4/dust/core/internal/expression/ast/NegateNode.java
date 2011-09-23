package se.l4.dust.core.internal.expression.ast;

/**
 * Node that negates the result of another node.
 * 
 * @author Andreas Holstenson
 *
 */
public class NegateNode
	implements Node
{
	private final Node node;

	public NegateNode(Node node)
	{
		this.node = node;
	}
	
	public Node getNode()
	{
		return node;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
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
		NegateNode other = (NegateNode) obj;
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
