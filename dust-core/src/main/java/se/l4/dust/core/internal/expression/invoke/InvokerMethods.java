package se.l4.dust.core.internal.expression.invoke;

public class InvokerMethods
{
	private InvokerMethods()
	{
	}
	
	public static boolean equals(Object o1, Object o2)
	{
		if(o1 == null && o2 == null)
		{
			return true;
		}
		else if(o1 == null || o2 == null)
		{
			return false;
		}
		
		return o1.equals(o2);
	}
}
