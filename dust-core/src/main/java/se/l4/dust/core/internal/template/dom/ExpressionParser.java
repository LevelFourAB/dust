package se.l4.dust.core.internal.template.dom;

import java.util.ArrayList;
import java.util.List;

import org.jdom.Content;
import org.jdom.Text;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import se.l4.crayon.Environment;
import se.l4.dust.api.TemplateManager;
import se.l4.dust.api.template.PropertySource;
import se.l4.dust.dom.Element;

@Singleton
public class ExpressionParser
{
	private final TemplateManager manager;
	private final Environment env;

	@Inject
	public ExpressionParser(TemplateManager manager, Environment env)
	{
		this.manager = manager;
		this.env = env;
	}
	
	public List<Content> parse(String value, Element parent)
	{
		List<Content> content = new ArrayList<Content>();
		
		ParserState state = ParserState.WAITING;
		
		StringBuffer buffer = new StringBuffer(0);
		String namespace = null;
		
		for(int i=0, n=value.length(); i<n; i++)
		{
			char c = value.charAt(i);
			
			switch(state)
			{
				case WAITING:
					if(c == '$')
					{
						namespace = null;
						state = ParserState.OPEN_BRACKET;
					}
					else
					{
						buffer.append(c);
					}
					break;
					
				case OPEN_BRACKET:
					if(c == '{')
					{
						// Expression started
						if(buffer.length() > 0)
						{
							content.add(new Text(buffer.toString()));
						}
						
						buffer.setLength(0);
						state = ParserState.NAMESPACE;
					}
					else
					{
						// Aborted expression
						buffer.append(c);
						state = ParserState.WAITING;
					}
					break;
					
				case NAMESPACE:
					if(c == '}')
					{
						// Namespace was actually the entire expression
						content.add(
							handlePropertyCreation(parent, namespace, buffer.toString())
						);
						buffer.setLength(0);
						state = ParserState.WAITING;
					}
					else if(c == ':')
					{
						// Namespace has been found, check the buffer to ensure that the namespace is valid
						namespace = buffer.toString();
						buffer.setLength(0);
						
						state = ParserState.CLOSE_BRACKET;
					}
					else if(Character.isLetterOrDigit(c))
					{
						buffer.append(c);
					}
					else
					{
						// Only letters and digits are allowed in the namespace, so revert to "normal" expression
						buffer.append(c);
						state = ParserState.CLOSE_BRACKET;
					}
					
					break;
					
				case CLOSE_BRACKET:
					if(c == '}')
					{
						content.add(
							handlePropertyCreation(parent, namespace, buffer.toString())
						);
						buffer.setLength(0);
						state = ParserState.WAITING;
					}
					else
					{
						buffer.append(c);
					}
					break;
			}
		}
		
		if(state != ParserState.WAITING)
		{
			TemplateUtils.throwException(parent, "Unclosed expression (${expression})");
		}
		else
		{
			if(buffer.length() > 0)
			{
				content.add(new Text(buffer.toString()));
			}
		}
		
		return content;
	}
	
	private Content handlePropertyCreation(Element parent, String namespace, String content)
	{
		if(namespace != null)
		{
			PropertySource source = manager.getPropertySource(namespace);
			if(source == null)
			{
				TemplateUtils.throwException(parent, "No property namespace called " + namespace);
			}
			
			return source.getPropertyContent(content, parent);
		}
		else
		{
			return parseExpression(parent, namespace, content);
		}
	}
	
	/**
	 * Parse an expression into {@link ExpressionNode}.
	 * 
	 * @param parent
	 * @param namespace
	 * @param content
	 * @return
	 */
	public ExpressionNode parseExpression(Element parent, String namespace, String content)
	{
		return new ExpressionNode(null, TemplateUtils.getLine(parent), content, env == Environment.DEVELOPMENT);
	}
	
	private enum ParserState
	{
		WAITING,
		OPEN_BRACKET,
		NAMESPACE,
		CLOSE_BRACKET
	}
}
