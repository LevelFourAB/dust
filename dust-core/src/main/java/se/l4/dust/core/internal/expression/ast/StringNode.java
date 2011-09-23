package se.l4.dust.core.internal.expression.ast;


/**
 * Node that holds a static string value.
 * 
 * @author Andreas Holstenson
 *
 */
public class StringNode
	extends AbstractNode
{
	private final String value;

	public StringNode(int line, int position, String value)
	{
		super(line, position);
		
		this.value = value;
	}
	
	/**
	 * Decode a string that contains escape characters.
	 * 
	 * @param in
	 * @return
	 */
	public static String decode(String in)
	{
		StringBuilder result = new StringBuilder(in.length());
		for(int i=0, n=in.length(); i<n; i++)
		{
			char c = in.charAt(i);
			if(c == '\\')
			{
				// Encoded character, do some skipping
				c = in.charAt(++i);
				switch(c)
				{
					case '\'':
						result.append('\'');
						break;
					case '"':
						result.append('"');
						break;
					case '\\':
						result.append('\\');
						break;
					case 'b':
						result.append('\b');
						break;
					case 't':
						result.append('\t');
						break;
					case 'n':
						result.append('\n');
						break;
					case 'f':
						result.append('\f');
						break;
					case 'r':
						result.append('\r');
						break;
					case 'u':
						// Unicode, read 4 chars and treat as hex
						if(i + 4 >= n)
							throw new IllegalArgumentException("Invalid escape sequence");
						
						String s = in.substring(i+1, i+5);
						result.append((char) Integer.parseInt(s, 16));
						i += 4;
						break;
				}
			}
			else
			{
				result.append(c);
			}
		}
		
		return result.toString();
	}
	
	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "[" + value + "]";
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj)
			return true;
		if(obj == null)
			return false;
		if(getClass() != obj.getClass())
			return false;
		StringNode other = (StringNode) obj;
		if(value == null)
		{
			if(other.value != null)
				return false;
		}
		else if(!value.equals(other.value))
			return false;
		return true;
	}
}
