package se.l4.dust.dom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jaxen.Context;
import org.jaxen.Function;
import org.jaxen.FunctionCallException;
import org.jaxen.XPathFunctionContext;
import org.jaxen.function.StringFunction;
import org.jdom.JDOMException;
import org.jdom.xpath.XPath;

public class XPathScraper
{
	private static Map<String, XPath> paths = new ConcurrentHashMap<String, XPath>();
	
	static
	{
		XPathFunctionContext ctx = (XPathFunctionContext) XPathFunctionContext.getInstance();
		ctx.registerFunction(null, "matches", new MatchesFunction(false));
		ctx.registerFunction(null, "find", new MatchesFunction(true));
		ctx.registerFunction(null, "split", new SplitFunction());
	}
	
	public static XPath getXPath(String path)
	{
		XPath p = paths.get(path);
		if(p == null)
		{
			try
			{
				p = XPath.newInstance(path);
				paths.put(path, p);
			}
			catch(JDOMException e)
			{
				// Can this happen?
				e.printStackTrace();
			}
		}
		
		return p;
	}
	
	private static class MatchesFunction
		implements Function
	{
		private boolean find;
		
		public MatchesFunction(boolean find)
		{
			this.find = find;
		}
		
		public Object call(Context arg0, List arg1)
			throws FunctionCallException
		{
			if(arg1.size() != 2)
			{
				throw new FunctionCallException((find ? "find" : "matches") + "() needs two arguments");
			}
			
			Object in = arg1.get(0);
			Object regex = arg1.get(1);
			
			String s = StringFunction.evaluate(in, arg0.getNavigator());
			String r = StringFunction.evaluate(regex, arg0.getNavigator());
			
			Pattern pattern = Pattern.compile(r);
			Matcher matcher = pattern.matcher(s);
			
			List<String> result = new ArrayList<String>(matcher.groupCount());
			
			if(find)
			{
				while(matcher.find())
				{
					result.add(matcher.group(0));
					
					for(int i=1, n=matcher.groupCount(); i<=n; i++)
					{
						result.add(matcher.group(i));
					}
				}
				
				return result;
			}
			else
			{
				if(matcher.matches())
				{
					if(matcher.groupCount() == 0)
					{
						return true;
					}
					else
					{
						for(int i=1, n=matcher.groupCount(); i<=n; i++)
						{
							result.add(matcher.group(i));
						}
						
						return result;
					}
				}
				
				return false;
			}
		}
	}
	
	private static class SplitFunction
		implements Function
	{
		public Object call(Context arg0, List arg1)
			throws FunctionCallException
		{
			if(arg1.size() != 2)
			{
				throw new FunctionCallException("split() needs two arguments");
			}
			
			Object str = arg1.get(0);
			Object split = arg1.get(1);
			
			String s = StringFunction.evaluate(split, arg0.getNavigator());
			String r = StringFunction.evaluate(str, arg0.getNavigator());
			
			return Arrays.asList(r.split(s));
		}
	}
}
