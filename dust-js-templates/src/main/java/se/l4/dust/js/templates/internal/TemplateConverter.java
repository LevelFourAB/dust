package se.l4.dust.js.templates.internal;

import se.l4.dust.api.TemplateException;
import se.l4.dust.api.expression.Expression;
import se.l4.dust.api.template.RenderingContext;
import se.l4.dust.api.template.dom.Comment;
import se.l4.dust.api.template.dom.Content;
import se.l4.dust.api.template.dom.DynamicContent;
import se.l4.dust.api.template.dom.Element;
import se.l4.dust.api.template.dom.Element.Attribute;
import se.l4.dust.api.template.dom.ParsedTemplate;
import se.l4.dust.api.template.dom.Text;
import se.l4.dust.core.internal.expression.ExpressionParser;
import se.l4.dust.core.internal.expression.ast.AddNode;
import se.l4.dust.core.internal.expression.ast.AndNode;
import se.l4.dust.core.internal.expression.ast.ArrayNode;
import se.l4.dust.core.internal.expression.ast.ChainNode;
import se.l4.dust.core.internal.expression.ast.DivideNode;
import se.l4.dust.core.internal.expression.ast.DoubleNode;
import se.l4.dust.core.internal.expression.ast.EqualsNode;
import se.l4.dust.core.internal.expression.ast.GreaterNode;
import se.l4.dust.core.internal.expression.ast.GreaterOrEqualNode;
import se.l4.dust.core.internal.expression.ast.IdentifierNode;
import se.l4.dust.core.internal.expression.ast.IndexNode;
import se.l4.dust.core.internal.expression.ast.InvokeNode;
import se.l4.dust.core.internal.expression.ast.KeywordNode;
import se.l4.dust.core.internal.expression.ast.LessNode;
import se.l4.dust.core.internal.expression.ast.LessOrEqualNode;
import se.l4.dust.core.internal.expression.ast.LongNode;
import se.l4.dust.core.internal.expression.ast.ModuloNode;
import se.l4.dust.core.internal.expression.ast.MultiplyNode;
import se.l4.dust.core.internal.expression.ast.NegateNode;
import se.l4.dust.core.internal.expression.ast.Node;
import se.l4.dust.core.internal.expression.ast.NotEqualsNode;
import se.l4.dust.core.internal.expression.ast.OrNode;
import se.l4.dust.core.internal.expression.ast.SignNode;
import se.l4.dust.core.internal.expression.ast.StringNode;
import se.l4.dust.core.internal.expression.ast.SubtractNode;
import se.l4.dust.core.internal.expression.ast.TernaryNode;
import se.l4.dust.core.internal.template.components.IfComponent;
import se.l4.dust.core.internal.template.components.LoopComponent;
import se.l4.dust.core.internal.template.components.ParameterComponent;
import se.l4.dust.core.internal.template.components.RawComponent;
import se.l4.dust.core.internal.template.dom.ExpressionContent;

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
	
	private void pushRaw(String value)
	{
		if(state == State.EXPRESSION)
		{
			builder.append("p.push('");
		}
		
		state = State.STRING;
		builder.append(value);
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
		else if(content instanceof RawComponent)
		{
			Attribute attr = ((RawComponent) content).getAttribute("value");
			Content[] value = attr.getValue();
			if(value != null && value.length > 0)
			{
				for(Content c : value)
				{
					if(c instanceof ExpressionContent)
					{
						handleExpression(c);
					}
					else if(c instanceof DynamicContent)
					{
						DynamicContent dc = (DynamicContent) c;
						Object v = dc.getValue(context, null);
						String string = context.getStringValue(v);
						pushRaw(string);
					}
					else
					{
						String text = ((Text) c).getText();
						pushRaw(text);
					}
				}
			}
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
		else if(content instanceof ExpressionContent)
		{
			handleExpression(content);
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
			if(c instanceof ExpressionContent)
			{
				return translateExpression(c);
			}
		}
		
		return null;
	}

	private void handleExpression(Content c)
	{
		String translated = translateExpression(c);
		
		pushExpression("p.push(");
		pushExpression(translated);
		pushExpression(");");
	}
	
	private void transformIfComponent(Content content)
	{
		IfComponent ic = (IfComponent) content;
		ParameterComponent ec = ic.getParameter("else", false);
		pushExpression("if(");
		
		for(Content c : ic.getAttributeValue("test"))
		{
			if(c instanceof ExpressionContent)
			{
				String translated = translateExpression(c);
				pushExpression(translated);
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

	private String translateExpression(Content c)
	{
		Expression expression = ((ExpressionContent) c).getExpression();
		Node ast = ExpressionParser.parse(expression.getSource());
		String translated = translate(ast);
		return translated;
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
	
	public static String translate(Node node)
	{
		if(node instanceof AddNode)
		{
			AddNode ad = (AddNode) node;
			return "(" + translate(ad.getLeft()) + "+" + translate(ad.getRight()) + ")";
		}
		else if(node instanceof SubtractNode)
		{
			SubtractNode sd = (SubtractNode) node;
			return "(" + translate(sd.getLeft()) + "-" + translate(sd.getRight()) + ")";
		}
		else if(node instanceof DivideNode)
		{
			DivideNode sd = (DivideNode) node;
			return "(" + translate(sd.getLeft()) + "/" + translate(sd.getRight()) + ")";
		}
		else if(node instanceof MultiplyNode)
		{
			MultiplyNode sd = (MultiplyNode) node;
			return "(" + translate(sd.getLeft()) + "*" + translate(sd.getRight()) + ")";
		}
		else if(node instanceof ModuloNode)
		{
			ModuloNode sd = (ModuloNode) node;
			return "(" + translate(sd.getLeft()) + "%" + translate(sd.getRight()) + ")";
		}
		else if(node instanceof DoubleNode)
		{
			DoubleNode dn = (DoubleNode) node;
			return String.valueOf(dn.getValue());
		}
		else if(node instanceof LongNode)
		{
			return String.valueOf(((LongNode) node).getValue());
		}
		else if(node instanceof StringNode)
		{
			StringBuilder out = new StringBuilder();
			String value = ((StringNode) node).getValue();
			out.append("'");
			encode(value, out);
			out.append("'");
			return out.toString();
		}
		else if(node instanceof KeywordNode)
		{
			KeywordNode kn = (KeywordNode) node;
			switch(kn.getType())
			{
				case FALSE:
					return "false";
				case TRUE:
					return "true";
				case NULL:
					return "null";
				case THIS:
					// XXX: What should we do with this one?
					return "";
			}
		}
		else if(node instanceof ChainNode)
		{
			ChainNode chain = (ChainNode) node;
			
			return translate(chain.getLeft()) + "." + translate(chain.getRight());
		}
		else if(node instanceof IdentifierNode)
		{
			IdentifierNode id = (IdentifierNode) node;
			if(id.getNamespace() != null)
			{
				// Namespaces are mapped to "native" JS-functions
				StringBuilder builder = new StringBuilder();
				builder.append("dust.namespace['");
				encode(id.getNamespace(), builder);
				builder.append("']['");
				encode(id.getIdentifier(), builder);
				builder.append("']");
				
				// TODO: Properties that are invoked on an instance
			}
			else
			{
				return id.getIdentifier();
			}
		}
		else if(node instanceof InvokeNode)
		{
			throw new TemplateException("Invocation of methods is not yet supported by JS templates");
		}
		else if(node instanceof IndexNode)
		{
			IndexNode in = (IndexNode) node;
			StringBuilder builder = new StringBuilder();
			
			builder.append(translate(in.getLeft()));
			
			for(Node n : in.getIndexes())
			{
				builder
					.append("[")
					.append(translate(n))
					.append("]");
			}
			
			return builder.toString();
		}
		else if(node instanceof ArrayNode)
		{
			ArrayNode an = (ArrayNode) node;
			Node[] values = an.getValues();
			StringBuilder builder = new StringBuilder()
				.append("[");
			
			for(int i=0, n=values.length; i<n; i++)
			{
				if(i > 0) builder.append(",");
				
				builder.append(translate(values[i]));
			}
			
			builder.append("]");
		}
		else if(node instanceof AndNode)
		{
			AndNode and = (AndNode) node;
			return "(" + translate(and.getLeft()) + " && " + translate(and.getRight()) + ")";
		}
		else if(node instanceof OrNode)
		{
			OrNode and = (OrNode) node;
			return "(" + translate(and.getLeft()) + " || " + translate(and.getRight()) + ")";
		}
		else if(node instanceof EqualsNode)
		{
			EqualsNode equals = (EqualsNode) node;
			return "(" + translate(equals.getLeft()) + "==" + translate(equals.getRight()) + ")";
		}
		else if(node instanceof NotEqualsNode)
		{
			NotEqualsNode ne = (NotEqualsNode) node;
			return "(" + translate(ne.getLeft()) + "!=" + translate(ne.getRight()) + ")";
		}
		else if(node instanceof GreaterNode)
		{
			GreaterNode gt = (GreaterNode) node;
			return "(" + translate(gt.getLeft()) + ">" + translate(gt.getRight()) + ")";
		}
		else if(node instanceof GreaterOrEqualNode)
		{
			GreaterOrEqualNode gt = (GreaterOrEqualNode) node;
			return "(" + translate(gt.getLeft()) + ">=" + translate(gt.getRight()) + ")";
		}
		else if(node instanceof LessNode)
		{
			LessNode lt = (LessNode) node;
			return "(" + translate(lt.getLeft()) + "<" + translate(lt.getRight()) + ")";
		}
		else if(node instanceof LessOrEqualNode)
		{
			LessOrEqualNode lt = (LessOrEqualNode) node;
			return "(" + translate(lt.getLeft()) + "<=" + translate(lt.getRight()) + ")";
		}
		else if(node instanceof GreaterNode)
		{
			GreaterNode gt = (GreaterNode) node;
			return "(" + translate(gt.getLeft()) + ">" + translate(gt.getRight()) + ")";
		}
		else if(node instanceof NegateNode)
		{
			return "!" + translate(((NegateNode) node).getNode());
		}
		else if(node instanceof SignNode)
		{
			SignNode sn = (SignNode) node;
			return (sn.isNegative() ? "-" : "+") + translate(sn.getNode());
		}
		else if(node instanceof TernaryNode)
		{
			TernaryNode tn = (TernaryNode) node;
			return translate(tn.getTest()) + " ? " 
				+ translate(tn.getLeft()) + " : "
				+ (tn.getRight() == null ? "null" : translate(tn.getRight()));
		}
		
		throw new IllegalArgumentException("Unknown node " + node.getClass());
	}
}
