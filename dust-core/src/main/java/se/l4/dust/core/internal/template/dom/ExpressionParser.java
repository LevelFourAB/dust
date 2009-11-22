package se.l4.dust.core.internal.template.dom;

import java.util.ArrayList;
import java.util.List;

import org.jdom.Content;
import org.jdom.Text;

import se.l4.dust.api.TemplateException;

public class ExpressionParser
{
	public static List<Content> parse(String value)
	{
		List<Content> content = new ArrayList<Content>();
		
		ParserState state = ParserState.WAITING;
		StringBuffer buffer = new StringBuffer(0);
		for(int i=0, n=value.length(); i<n; i++)
		{
			char c = value.charAt(i);
			
			if(state == ParserState.WAITING)
			{
				if(c == '$')
				{
					state = ParserState.OPEN_BRACKET;
				}
				else
				{
					buffer.append(c);
				}
			}
			else if(state == ParserState.OPEN_BRACKET)
			{
				if(c == '{')
				{
					// Expression started
					if(buffer.length() > 0)
					{
						content.add(new Text(buffer.toString()));
					}
					
					buffer.setLength(0);
					state = ParserState.CLOSE_BRACKET;
				}
				else
				{
					// Aborted expression
					buffer.append(c);
					state = ParserState.WAITING;
				}
			}
			else if(state == ParserState.CLOSE_BRACKET)
			{
				if(c == '}')
				{
					content.add(new ExpressionNode(buffer.toString()));
					buffer.setLength(0);
					state = ParserState.WAITING;
				}
				else
				{
					buffer.append(c);
				}
			}
		}
		
		if(state != ParserState.WAITING)
		{
			throw new TemplateException("Unclosed expression (${expression})");
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
	
	private enum ParserState
	{
		WAITING,
		OPEN_BRACKET,
		CLOSE_BRACKET
	}
}
