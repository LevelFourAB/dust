package se.l4.dust.api.template.spi;

import java.util.ArrayList;
import java.util.List;

import se.l4.dust.api.resource.ResourceLocation;
import se.l4.dust.api.template.TemplateBuilder;
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
	public List<Content> parse(ResourceLocation source, int line, int column, CharSequence value)
	{
		List<Content> content = new ArrayList<Content>();
		
		ParserState state = ParserState.WAITING;
		
		StringBuilder buffer = new StringBuilder();

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
						
						state = ParserState.MAIN;
								
						if(buffer.length() > 0)
						{
							Text text = new Text(buffer.toString());
							text.withDebugInfo(source, line, column);
							content.add(text);
						}
								
						buffer.setLength(0);
					}
					else
					{
						buffer.append(c);
					}
					break;
					
				case MAIN:
					if(isLocatedAt(value, i, endToken))
					{
						int jump = endToken.length() - 1;
						i += jump;
						column += jump;
						
						try
						{
							Content dynamicContent = builder.createDynamicContent(buffer.toString());
							dynamicContent.withDebugInfo(source, currentExpressionStartLine, currentExpressionStart);
							content.add(dynamicContent);
						}
						catch(Throwable e)
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
				Text text = new Text(buffer.toString());
				text.withDebugInfo(source, line, column);
				content.add(text);
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
		MAIN
	}
}
