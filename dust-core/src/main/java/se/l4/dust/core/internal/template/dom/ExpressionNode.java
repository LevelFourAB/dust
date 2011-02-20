package se.l4.dust.core.internal.template.dom;

import java.io.Serializable;

import org.mvel2.CompileException;
import org.mvel2.MVEL;
import org.mvel2.ParserContext;

import se.l4.dust.api.TemplateException;
import se.l4.dust.api.template.PropertyContent;
import se.l4.dust.api.template.RenderingContext;

public class ExpressionNode
	extends PropertyContent
{
	private Serializable expression;
	private Serializable setter;
	private final String file;
	private final int line;
	private final String rawExpression;
	
	public ExpressionNode(String file, int line, String expression, boolean debug)
	{
		this.file = file;
		this.line = line;
		
		ParserContext ctx = new ParserContext();
		ctx.addImport("Escape", EscapeHelper.class);
		
		this.rawExpression = debug ? expression : null; 
		
		try
		{
			this.expression = MVEL.compileExpression(expression, ctx);
			this.setter = MVEL.compileSetExpression(expression, ctx);
		}
		catch(Throwable t)
		{
			throw fail(t);
		}		
	}
	
	@Override
	public Object getValue(RenderingContext ctx, Object root)
	{
		try
		{
			return MVEL.executeExpression(expression, root);
		}
		catch(CompileException e)
		{
			throw fail(e);
		}
	}
	
	@Override
	public void setValue(RenderingContext ctx, Object root, Object value)
	{
		try
		{
			MVEL.executeSetExpression(setter, root, value);
		}
		catch(CompileException e)
		{
			throw fail(e);
		}
	}
	
	private TemplateException fail(Throwable t)
	{
		if(rawExpression != null)
		{
			return new TemplateException(
				"Error near line " + line 
				+ " for ${" + rawExpression + "}:" + t.getMessage(), t
			);
		}
		else
		{
			return new TemplateException("Error in near line " + line + ": " + t.getMessage(), t);
		}
	}
	
	@Override
	public String toString()
	{
		return "ExpressionNode[" + expression + "]";
	}
}
