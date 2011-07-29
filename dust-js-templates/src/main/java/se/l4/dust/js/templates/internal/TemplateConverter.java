package se.l4.dust.js.templates.internal;

import se.l4.dust.api.template.RenderingContext;
import se.l4.dust.api.template.dom.Comment;
import se.l4.dust.api.template.dom.Content;
import se.l4.dust.api.template.dom.DynamicContent;
import se.l4.dust.api.template.dom.Element;
import se.l4.dust.api.template.dom.Element.Attribute;
import se.l4.dust.api.template.dom.ParsedTemplate;
import se.l4.dust.api.template.dom.Text;
import se.l4.dust.core.internal.template.components.IfComponent;
import se.l4.dust.core.internal.template.components.LoopComponent;
import se.l4.dust.core.internal.template.components.ParameterComponent;
import se.l4.dust.core.internal.template.expression.MvelPropertySource;

/**
 * Converter of {@link ParsedTemplate} into a JavaScript based function.
 * 
 * @author Andreas Holstenson
 *
 */
public class TemplateConverter
{
	private enum State
	{
		EXPRESSION,
		STRING
	}
	
	private final ParsedTemplate template;
	private final RenderingContext context;
	
	private StringBuilder builder;
	private State state;
	
	private int loop;

	public TemplateConverter(ParsedTemplate template, RenderingContext context)
	{
		this.template = template;
		this.context = context;
	}
	
	public String transform()
	{
		builder = new StringBuilder();
		builder.append("function(data) {\nvar p=[];with(data) {\n");
		state = State.EXPRESSION;
		
		transform(template.getRoot());
		
		end();
		
		builder.append("\n} return p.join(''); }");
		return builder.toString();
	}
	
	private void pushString(String value)
	{
		if(state == State.EXPRESSION)
		{
			builder.append("p.push('");
		}
		
		state = State.STRING;
		encode(value, builder);
	}
	
	private void pushExpression(String expression)
	{
		if(state == State.STRING)
		{
			builder.append("');\n");
		}
		
		state = State.EXPRESSION;
		builder.append(expression);
	}
	
	private void end()
	{
		if(state == State.STRING)
		{
			builder.append("');");
		}
	}
	
	private void transform(Content content)
	{
		if(content instanceof IfComponent)
		{
			transformIfComponent(content);
		}
		else if(content instanceof LoopComponent)
		{
			LoopComponent lc = (LoopComponent) content;
			String source = getExpression(lc.getSource());
			String value = getExpression(lc.getValue());
			
			loop++;
			String loopIndex = "__i" + loop;
			String countIndex = "__n" + loop;
			
			pushExpression("for(var ");
			pushExpression(loopIndex);
			pushExpression("=0, ");
			pushExpression(countIndex);
			pushExpression("=");
			pushExpression(source);
			pushExpression(".length; ");
			pushExpression(loopIndex);
			pushExpression("<");
			pushExpression(countIndex);
			pushExpression("; ");
			pushExpression(loopIndex);
			pushExpression("++) {\n");
			
			pushExpression("var ");
			pushExpression(value);
			pushExpression("=");
			pushExpression(source);
			pushExpression("[");
			pushExpression(loopIndex);
			pushExpression("];\n");
			
			for(Content c : lc.getRawContents())
			{
				transform(c);
			}
			
			pushExpression("}");
		}
		else if(content instanceof Element)
		{
			Element el = (Element) content;
			pushString("<");
			pushString(el.getName());
			
			Attribute[] attrs = el.getAttributes();
			if(attrs.length > 0)
			{
				for(int i=0, n=attrs.length; i<n; i++)
				{
					pushString(" ");
					pushString(attrs[i].getName());
					
					Content[] value = attrs[i].getValue();
					if(value != null && value.length > 0)
					{
						pushString("=\"");
						for(Content c : value)
						{
							transform(c);
						}
						pushString("\"");
					}
				}
			}
			
			pushString(">");
			
			for(Content c : el.getRawContents())
			{
				transform(c);
			}
			
			pushString("</");
			pushString(el.getName());
			pushString(">");
		}
		else if(content instanceof Comment)
		{
			Comment comment = (Comment) content;
			pushString("<!--");
			
			for(Content c : comment.getRawContents())
			{
				transform(c);
			}
			
			pushString("-->");
		}
		else if(content instanceof MvelPropertySource.Content)
		{
			// MVEL Expression, try to handle as JavaScript
			String expression = ((MvelPropertySource.Content) content).getExpression();
			pushExpression("p.push(");
			pushExpression(expression);
			pushExpression(");");
		}
		else if(content instanceof DynamicContent)
		{
			DynamicContent dc = (DynamicContent) content;
			Object value = dc.getValue(context, null);
			String string = context.getStringValue(value);
			pushString(string);
		}
		else if(content instanceof Text)
		{
			String text = ((Text) content).getText();
			pushString(text);
		}
	}
	
	private String getExpression(Attribute attr)
	{
		for(Content c : attr.getValue())
		{
			if(c instanceof MvelPropertySource.Content)
			{
				return ((MvelPropertySource.Content) c).getExpression();
			}
		}
		
		return null;
	}

	private void transformIfComponent(Content content)
	{
		IfComponent ic = (IfComponent) content;
		ParameterComponent ec = ic.getParameter("else", false);
		pushExpression("if(");
		
		for(Content c : ic.getAttributeValue("test"))
		{
			if(c instanceof MvelPropertySource.Content)
			{
				String expression = ((MvelPropertySource.Content) c).getExpression();
				pushExpression(expression);
			}
			else
			{
				transform(c);
			}
		}
		
		pushExpression(") {\n");

		// Output the contents of the if-statement
		for(Content c : ic.getRawContents())
		{
			if(c != ec) transform(c);
		}
		
		if(ec != null)
		{
			// Push the else expression
			pushExpression("} else {\n");
			
			for(Content c : ec.getRawContents())
			{
				transform(c);
			}
		}
		
		pushExpression("}\n");
	}
	
	private static void encode(String in, StringBuilder out)
	{
		for(int i=0, n=in.length(); i<n; i++)
		{
			char c = in.charAt(i);
			switch(c)
			{
				case '\'':
					out.append("\\'");
					break;
				case '"':
					out.append("\\\"");
					break;
				case '\\':
					out.append("\\\\");
					break;
				case '/':
					out.append("\\/");
					break;
				case '\r':
					out.append("\\r");
					break;
				case '\n':
					out.append("\\n");
					break;
				case '\t':
					out.append("\\t");
					break;
				case '\b':
					out.append("\\b");
					break;
				case '\f':
					out.append("\\f");
					break;
				default:
					if(c < 32 || c > 0x7f)
					{
						if(c > 0xfff)
						{
							out.append("\\u");
						}
						else if(c > 0xff)
						{
							out.append("\\u0");
						}
						else if(c > 0xf)
						{
							out.append("\\u00");
						}
						else
						{
							out.append("\\u000");
						}
						
						out.append(Integer.toHexString(c).toUpperCase());
					}
					else
					{
						out.append(c);
					}
			}
		}
	}
}
