package se.l4.dust.jaxrs;

import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import se.l4.dust.jaxrs.spi.Context;

import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Scope;

/**
 * Container of available scopes that are suitable for webapps.
 * 
 * @author Andreas Holstenson
 *
 */
public class WebScopes
{
	private static String KEY = "";
	private static final AtomicReference<Context> context;
	
	static
	{
		context = new AtomicReference<Context>();
	}
	
	public static void setContext(Context ctx)
	{
		context.set(ctx);
	}
	
	public static final Scope REQUEST = new Scope()
	{
		public <T> Provider<T> scope(final Key<T> key, final Provider<T> p)
		{
			return new Provider<T>()
			{
				@SuppressWarnings("unchecked")
				public T get()
				{
					HttpServletRequest req = context.get().getHttpServletRequest();
					
					if(req == null)
					{
//						throw new OutOfScopeException("Request scoped objects can only be used within HTTP requests");
					}
					
					String localKey = KEY + key.toString();
					
					synchronized(req)
					{
						Object o = req.getAttribute(localKey);
						if(o == null)
						{
							o = p.get();
							req.setAttribute(localKey, o);
						}
						
						return (T) o;
					}
				}
				
				@Override
				public String toString()
				{
					return "WebScopes.REQUEST";
				}
			};
		}
	};
	
	public static final Scope SESSION = new Scope()
	{
		public <T> Provider<T> scope(final Key<T> key, final Provider<T> p)
		{
			return new Provider<T>()
			{
				@SuppressWarnings("unchecked")
				public T get()
				{
					HttpSession session = context.get().getHttpSession();
					
					synchronized(session)
					{
						String localKey = KEY + key.toString();
						
						Object o = session.getAttribute(localKey);
						if(o == null)
						{
							o = p.get();
							session.setAttribute(localKey, o);
						}
						
						return (T) o;
					}
				}
				
				@Override
				public String toString()
				{
					return "WebScopes.SESSION";
				}
			};
		}
	};
	
	public static final Scope CONTEXT = new Scope()
	{
		public <T> Provider<T> scope(final Key<T> key, final Provider<T> p)
		{
			return new Provider<T>()
			{
				@SuppressWarnings("unchecked")
				public T get()
				{
					ServletContext ctx = context.get().getServletContext();
					
					synchronized(ctx)
					{
						String localKey = KEY + key.toString();
						
						Object o = ctx.getAttribute(localKey);
						if(o == null)
						{
							o = p.get();
							ctx.setAttribute(localKey, o);
						}
						
						return (T) o;
					}
				}
				
				@Override
				public String toString()
				{
					return "WebScopes.CONTEXT";
				}
			};
		}
	};
	
	private WebScopes()
	{
	}
}
