package se.l4.dust.api;

import se.l4.dust.api.annotation.ContextScoped;

import com.google.inject.Key;
import com.google.inject.OutOfScopeException;
import com.google.inject.Provider;
import com.google.inject.Scope;

/**
 * Custom scopes available in Dust.
 * 
 * @author Andreas Holstenson
 *
 */
public class Scopes
{
	private static final ThreadLocal<Context> context;
	
	static
	{
		context = new ThreadLocal<Context>();
	}
	
	private Scopes() {}
	
	/**
	 * Get the active context.
	 * 
	 * @return
	 */
	public static Context getActiveContext()
	{
		return context.get();
	}
	
	/**
	 * Clear the active context.
	 * 
	 */
	public static void clearActiveContext()
	{
		context.remove();
	}
	
	/**
	 * Set the active context.
	 * 
	 * @param ctx
	 */
	public static void setActiveContext(Context ctx)
	{
		context.set(ctx);
	}
	
	/**
	 * Scope for {@link ContextScoped}.
	 */
	public static final Scope CONTEXT = new Scope()
	{
		public <T> Provider<T> scope(final Key<T> key, final Provider<T> p)
		{
			return new Provider<T>()
			{
				@SuppressWarnings("unchecked")
				public T get()
				{
					Context ctx = getActiveContext();
					
					if(ctx == null)
					{
						throw new OutOfScopeException("There is no active context");
					}
					
					Object o = ctx.getValue(key);
					if(o == null)
					{
						o = p.get();
						ctx.putValue(key, o);
					}
						
					return (T) o;
				}
				
				@Override
				public String toString()
				{
					return "Scopes.CONTEXT";
				}
			};
		}
	};
}
