package se.l4.dust.core.internal.template.dom;

import java.io.Serializable;

import org.mvel2.CompileException;
import org.mvel2.MVEL;
import org.mvel2.ParserContext;

import se.l4.dust.api.TemplateException;
import se.l4.dust.api.template.PropertyContent;
import se.l4.dust.core.template.EscapeHelper;

public class ExpressionNode
	extends PropertyContent
{
	private Serializable expression;
	private Serializable setter;
	private final String file;
	private final int line;
	
	public ExpressionNode(String file, int line, String expression)
	{
		this.file = file;
		this.line = line;
		
		ParserContext ctx = new ParserContext();
		ctx.addImport("Escape", EscapeHelper.class);
		ctx.setDebugSymbols(true);
		
		this.expression = MVEL.compileExpression(expression, ctx);
		this.setter = MVEL.compileSetExpression(expression, ctx);
	}
	
	@Override
	public Object getValue(Object root)
	{
		try
		{
			return MVEL.executeExpression(expression, root);
		}
		catch(CompileException e)
		{
			throw new TemplateException("Error in " + file + " near line " + line + ": " + e.getMessage(), e);
		}
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
	
	public static void main(String[] args)
	{
		String s = "kaka";
		
		ParserContext ctx = new ParserContext();
		ctx.addImport("Escape", EscapeHelper.class);
		
//		MVEL.eval("len()", s);
	
		try
		{
			Serializable expr = MVEL.compileExpression("len()", ctx);
			MVEL.executeExpression(expr, s);
		}
		catch(Exception e)
		{
			System.out.println(e.getClass());
			e.printStackTrace(System.out);
		}
	}
}
