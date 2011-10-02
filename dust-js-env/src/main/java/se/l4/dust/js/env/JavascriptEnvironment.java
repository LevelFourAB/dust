package se.l4.dust.js.env;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builder for creating a JavaScript environment that is suitable for running
 * scripts designed to be run in a browser.
 * 
 * @author Andreas Holstenson
 *
 */
public class JavascriptEnvironment
{
	/** Fragment that should be run in the context. */
	interface Fragment
	{
		void evaluate(Context context, Scriptable scope)
			throws IOException;
	}
	
	private List<Fragment> fragments;
	private boolean hasEvaluated;
	
	public JavascriptEnvironment()
	{
		this(null);
	}
	
	public JavascriptEnvironment(String name)
	{
		fragments = new ArrayList<Fragment>();
		Logger logger = name == null 
			? LoggerFactory.getLogger(JavascriptEnvironment.class)
			: LoggerFactory.getLogger(name);
				
		define("console", new JavascriptConsole(logger));
	}
	
	/**
	 * Add a url that should be evaluated as part of this environment.
	 * 
	 * @param url
	 * @return
	 */
	public JavascriptEnvironment add(final URL url)
	{
		fragments.add(new Fragment()
		{
			public void evaluate(Context context, Scriptable scope)
				throws IOException
			{
				InputStream stream = url.openStream();
				try
				{
					InputStreamReader in = new InputStreamReader(stream);
					context.evaluateReader(scope, in, url.getFile(), 1, null);
				}
				finally
				{
					try
					{
						stream.close();
					}
					catch(IOException e)
					{
					}
				}
			}
		});
		
		return this;
	}
	
	/**
	 * Add a JavaScript string fragment that should be evaluated.
	 * 
	 * @param fragment
	 * @return
	 */
	public JavascriptEnvironment add(final String fragment)
	{
		fragments.add(new Fragment()
		{
			public void evaluate(Context context, Scriptable scope)
				throws IOException
			{
				context.evaluateString(scope, fragment, "<fragment>", 1, null);
			}
		});
		
		return this;
	}
	
	/**
	 * Define a variable that can be accessed in the scripts.
	 * 
	 * @param variable
	 * @param value
	 * @return
	 */
	public JavascriptEnvironment define(final String variable, final Object value)
	{
		fragments.add(new Fragment()
		{
			public void evaluate(Context context, Scriptable scope)
				throws IOException
			{
				Object wrappedOut = Context.javaToJS(value, scope);
				ScriptableObject.putProperty(scope, variable, wrappedOut);
			}
		});
		
		return this;
	}
	
	/**
	 * Evaluate and return the result of the given fragment.
	 * 
	 * @param fragment
	 * @return
	 */
	public Object evaluate(final String fragment)
		throws IOException
	{
		class Holder { Object value; }
		final Holder holder = new Holder();
		fragments.add(new Fragment()
		{
			public void evaluate(Context context, Scriptable scope)
				throws IOException
			{
				holder.value = context.evaluateString(scope, fragment, "<fragment>", 1, null);
			}
		});
		
		evaluate();
		
		return holder.value;
	}

	public void evaluate()
		throws IOException
	{
		if(hasEvaluated) return;
		
		Context cx = Context.enter();
		cx.setOptimizationLevel(-1);
		cx.setLanguageVersion(Context.VERSION_1_8);
		try
		{
			Scriptable scope = cx.initStandardObjects();
		
			for(Fragment fragment : fragments)
			{
				fragment.evaluate(cx, scope);
			}
			
			hasEvaluated = true;
		}
		finally
		{
			Context.exit();
		}
	}
	
	public Object execute()
	{
		return null;
	}
}
