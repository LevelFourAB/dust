package se.l4.dust.core.internal.expression.ast;

import java.util.List;

/**
 * Node representing an invokation of a method. The id of the method is
 * stored in the node while the context of the invocation is given by its
 * container.
 *
 * @author Andreas Holstenson
 *
 */
public class InvokeNode
	extends AbstractNode
{
	private final IdentifierNode id;
	private final List<Node> parameters;

	public InvokeNode(int line, int position, IdentifierNode id, List<Node> parameters)
	{
		super(line, position);

		this.id = id;
		this.parameters = parameters;
	}

	public IdentifierNode getId()
	{
		return id;
	}

	public List<Node> getParameters()
	{
		return parameters;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "[id=" + id + ", parameters=" + parameters + "]";
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result
				+ ((parameters == null) ? 0 : parameters.hashCode());
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
		InvokeNode other = (InvokeNode) obj;
		if(id == null)
		{
			if(other.id != null)
				return false;
		}
		else if(!id.equals(other.id))
			return false;
		if(parameters == null)
		{
			if(other.parameters != null)
				return false;
		}
		else if(!parameters.equals(other.parameters))
			return false;
		return true;
	}
}
