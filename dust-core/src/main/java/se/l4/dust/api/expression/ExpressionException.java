package se.l4.dust.api.expression;

/**
 * Exception thrown on expression errors.
 * 
 * @author Andreas Holstenson
 *
 */
public class ExpressionException
	extends RuntimeException
{

	public ExpressionException(String source, int line, int position, String message)
	{
		super(constructError(source, line, position, message));
	}
	
	private static String constructError(String source, int line, int position, String message)
	{
		StringBuilder result = new StringBuilder();
		
		result.append("Error on line ")
			.append(line)
			.append(", column ")
			.append(position+1)
			.append(":\n");
		
		String[] lines = source.split("(\n|\r)");
		for(int i=0, n=lines.length; i<n; i++)
		{
			result.append("  ")
				.append(i+1)
				.append(": ")
				.append(lines[i])
				.append('\n');
			
			if(i == line-1)
			{
				result.append("     ");
				
				for(int j=0, m=position; j<m; j++)
				{
					result.append(' ');
				}
				
				result.append('^');
				result.append("\n");
				
				break;
			}
		}
		
		result.append("\n  ").append(message);
		
		return result.toString();
	}
}
