package se.l4.dust.core.internal.template.dom;

import java.io.Serializable;

import org.mvel2.MVEL;
import org.mvel2.ParserContext;

import se.l4.dust.api.template.PropertyContent;
import se.l4.dust.core.template.EscapeHelper;

public class ExpressionNode
	extends PropertyContent
{
	private Serializable expression;
	private Serializable setter;
	
	public ExpressionNode(String file, int line, String expression)
	{
		ParserContext ctx = new ParserContext();
		ctx.addImport("Escape", EscapeHelper.class);
		
		this.expression = MVEL.compileExpression(expression, ctx);
		this.setter = MVEL.compileSetExpression(expression, ctx);
	}
	
	@Override
	public Object getValue(Object root)
	{
		return MVEL.executeExpression(expression, root);
	}
	
	@Override
	public void setValue(Object root, Object value)
	{
		MVEL.executeSetExpression(setter, root, value);
	}
	
	@Override
	public String toString()
	{
		return "ExpressionNode[" + expression + "]";
	}
}
