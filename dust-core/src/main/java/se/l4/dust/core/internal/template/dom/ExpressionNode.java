package se.l4.dust.core.internal.template.dom;

import java.io.Serializable;

import org.jdom.Content;
import org.mvel2.MVEL;
import org.mvel2.ParserContext;

import se.l4.dust.core.template.EscapeHelper;

public class ExpressionNode
	extends Content
{
	private Serializable expression;
	private Serializable setter;
	
	public ExpressionNode(String expression)
	{
		ParserContext ctx = new ParserContext();
		ctx.addImport("Escape", EscapeHelper.class);
		
		this.expression = MVEL.compileExpression(expression, ctx);
		this.setter = MVEL.compileSetExpression(expression);
	}
	
	public Object getValue(Object root)
	{
		return MVEL.executeExpression(expression, root);
	}
	
	public void setValue(Object root, Object value)
	{
		MVEL.executeSetExpression(setter, root, value);
	}
	
	@Override
	public String getValue()
	{
		return null;
	}

	@Override
	public String toString()
	{
		return "ExpressionNode[" + expression + "]";
	}
}
