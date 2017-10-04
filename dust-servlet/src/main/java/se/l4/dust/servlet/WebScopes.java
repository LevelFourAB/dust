package se.l4.dust.servlet;

import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.inject.Key;
import com.google.inject.OutOfScopeException;
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
	private static String KEY = "dust.";
	private static final AtomicReference<ServletContext> context;
	private static final ThreadLocal<HttpServletRequest> request;
	private static final ThreadLocal<HttpServletResponse> response;

	static
	{
		context = new AtomicReference<>();
		request = new ThreadLocal<>();
		response = new ThreadLocal<>();
	}

	public static void setContext(ServletContext ctx)
	{
		context.set(ctx);
	}

	public static void init(HttpServletRequest req, HttpServletResponse resp)
	{
		request.set(req);
		response.set(resp);
	}

	public static void clear()
	{
		request.remove();
		response.remove();
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
					HttpServletRequest req = request.get();

					if(req == null)
					{
						throw new OutOfScopeException("Request scoped objects can only be used within HTTP requests; For " + key);
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
					HttpSession session = request.get().getSession();

					if(session == null)
					{
						throw new OutOfScopeException("Session scoped objects can only be used within HTTP requests");
					}

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

	private WebScopes()
	{
	}

	public static HttpServletRequest getRequest()
	{
		return request.get();
	}

	public static HttpServletResponse getResponse()
	{
		return response.get();
	}

	public static ServletContext getContext()
	{
		return context.get();
	}
}
