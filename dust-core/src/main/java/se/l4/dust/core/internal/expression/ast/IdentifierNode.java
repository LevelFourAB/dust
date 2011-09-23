package se.l4.dust.core.internal.expression.ast;

/**
 * Node representing an identifier, with an optional namespace.
 * 
 * @author Andreas Holstenson
 *
 */
public class IdentifierNode
	extends AbstractNode
{
	private final String namespace;
	private final String identifier;

	public IdentifierNode(int line, int position, String namespace, String identifier)
	{
		super(line, position);
		
		this.namespace = namespace;
		this.identifier = identifier;
	}
	
	public String getNamespace()
	{
		return namespace;
	}
	
	public String getIdentifier()
	{
		return identifier;
	}
	
	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "[ns=" + namespace + ", id=" + identifier + "]"; 
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((identifier == null) ? 0 : identifier.hashCode());
		result = prime * result
				+ ((namespace == null) ? 0 : namespace.hashCode());
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
		IdentifierNode other = (IdentifierNode) obj;
		if(identifier == null)
		{
			if(other.identifier != null)
				return false;
		}
		else if(!identifier.equals(other.identifier))
			return false;
		if(namespace == null)
		{
			if(other.namespace != null)
				return false;
		}
		else if(!namespace.equals(other.namespace))
			return false;
		return true;
	}
}
