package se.l4.dust.core.internal.expression.ast;

/**
 * Node that holds a double value.
 * 
 * @author Andreas Holstenson
 *
 */
public class DoubleNode
	implements Node
{
	private final double value;

	public DoubleNode(double value)
	{
		this.value = value;
	}
	
	public double getValue()
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
		long temp;
		temp = Double.doubleToLongBits(value);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		DoubleNode other = (DoubleNode) obj;
		if(Double.doubleToLongBits(value) != Double
				.doubleToLongBits(other.value))
			return false;
		return true;
	}
}
