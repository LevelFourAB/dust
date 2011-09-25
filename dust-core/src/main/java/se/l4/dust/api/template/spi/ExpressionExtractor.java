package se.l4.dust.api.template.spi;

import java.util.ArrayList;
import java.util.List;

import se.l4.dust.api.template.dom.Content;
import se.l4.dust.api.template.dom.Text;

/**
 * Extractor of expressions within a larger text mass. This is used by template
 * parsers to extract variables and expressions.
 * 
 * @author Andreas Holstenson
 *
 */
public class ExpressionExtractor
{
	private final String startToken;
	private final String endToken;
	private final TemplateBuilder builder;
	private final ErrorCollector errors;

	public ExpressionExtractor(String startToken, String endToken, TemplateBuilder builder,
			ErrorCollector errors)
	{
		this.startToken = startToken;
		this.endToken = endToken;
		this.builder = builder;
		this.errors = errors;
	}
	
	/**
	 * Parse a sequence of characters that have started on the current line
	 * and column.
	 * 
	 * @param line
	 * @param column
	 * @param value
	 * @return
	 */
	public List<Content> parse(int line, int column, CharSequence value)
	{
		List<Content> content = new ArrayList<Content>();
		
		ParserState state = ParserState.WAITING;
		
		StringBuilder buffer = new StringBuilder();
		String namespace = null;

		int currentExpressionStart = column;
		int currentExpressionStartLine = line;
		for(int i=0, n=value.length(); i<n; i++)
		{
			char c = value.charAt(i);
			if(c == '\n')
			{
				line++;
				column = 1;
			}
			else
			{
				column++;
			}
			
			switch(state)
			{
				case WAITING:
					if(isLocatedAt(value, i, startToken))
					{
						currentExpressionStart = column;
						currentExpressionStartLine = line;
						int jump = startToken.length() - 1;
						i += jump;
						column += jump;
						
						namespace = null;
						state = ParserState.NAMESPACE;
								
						if(buffer.length() > 0)
						{
							content.add(new Text(buffer.toString()));
						}
								
						buffer.setLength(0);
					}
					else
					{
						buffer.append(c);
					}
					break;
					
				case NAMESPACE:
					if(isLocatedAt(value, i, endToken))
					{
						int jump = endToken.length() - 1;
						i += jump;
						column += jump;
						
						// Namespace was actually the entire expression
						try
						{
							content.add(builder.createDynamicContent(namespace, buffer.toString()));
						}
						catch(Exception e)
						{
							errors.newError(currentExpressionStartLine, currentExpressionStart, "%s", e);
						}
						buffer.setLength(0);
						state = ParserState.WAITING;
					}
					else if(c == ':')
					{
						// Namespace has been found, check the buffer to ensure that the namespace is valid
						namespace = buffer.toString();
						buffer.setLength(0);
						
						state = ParserState.CLOSE;
					}
					else if(Character.isLetterOrDigit(c))
					{
						buffer.append(c);
					}
					else
					{
						// Only letters and digits are allowed in the namespace, so revert to "normal" expression
						buffer.append(c);
						state = ParserState.CLOSE;
					}
					
					break;
					
				case CLOSE:
					if(isLocatedAt(value, i, endToken))
					{
						int jump = endToken.length() - 1;
						i += jump;
						column += jump;
						
						try
						{
							content.add(builder.createDynamicContent(namespace, buffer.toString()));
						}
						catch(Exception e)
						{
							errors.newError(currentExpressionStartLine, currentExpressionStart, "%s", e);
						}
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
			errors.newError(currentExpressionStartLine, currentExpressionStart, "Unclosed expression");
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
	
	private boolean isLocatedAt(CharSequence sequence, int index, String token)
	{
		int tokenLength = token.length();
		
		if(index < 0 || (index > sequence.length() - tokenLength))
		{
			return false;
		}
		
		int offset = 0;
		while(--tokenLength >= 0)
		{
			if(sequence.charAt(index++) != token.charAt(offset++))
			{
				return false;
			}
		}
		
		return true;
	}
	
	private enum ParserState
	{
		WAITING,
		NAMESPACE,
		CLOSE
	}
}
