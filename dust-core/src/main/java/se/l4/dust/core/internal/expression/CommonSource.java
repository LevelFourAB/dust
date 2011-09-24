package se.l4.dust.core.internal.expression;

import se.l4.dust.api.expression.DynamicMethod;
import se.l4.dust.api.expression.DynamicProperty;
import se.l4.dust.api.expression.ExpressionEncounter;
import se.l4.dust.api.expression.ExpressionSource;
import se.l4.dust.api.expression.StaticProperty;
import se.l4.dust.api.template.dom.Element;

/**
 * Source of common properties and methods used within expressions.
 * 
 * @author Andreas Holstenson
 *
 */
public class CommonSource
	implements ExpressionSource
{

	@Override
	public DynamicProperty getProperty(ExpressionEncounter encounter, String name)
	{
		if(name.equals("skip"))
		{
			return new StaticProperty(Element.Attribute.ATTR_SKIP);
		}
		else if(name.equals("emit"))
		{
			return new StaticProperty(Element.Attribute.ATTR_EMIT);
		}
		
		return null;
	}

	@Override
	public DynamicMethod getMethod(ExpressionEncounter encounter, String name, Class... parameters)
	{
		// TODO Auto-generated method stub
		return null;
	}

	
}
