package se.l4.dust.api.template.spi;

import java.util.List;

import se.l4.dust.api.Value;
import se.l4.dust.api.Values;
import se.l4.dust.api.resource.ResourceLocation;
import se.l4.dust.api.template.TemplateBuilder;
import se.l4.dust.core.internal.expression.ExpressionDebugger;

import com.google.common.collect.Lists;

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
	public List<Value<?>> parse(ResourceLocation source, int line, int column, CharSequence value)
	{
		List<Value<?>> content = Lists.newArrayList();

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
							content.add(Values.of(buffer.toString()));
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
							Value<?> v = builder.createDynamicContent(buffer.toString());
							if(v instanceof ExpressionDebugger)
							{
								((ExpressionDebugger) v).withDebugInfo(source, currentExpressionStartLine, currentExpressionStart);
							}
							content.add(v);
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
				content.add(Values.of(buffer.toString()));
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
