package se.l4.dust.servlet.internal.routing;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.Filter;
import javax.servlet.Servlet;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import se.l4.dust.servlet.ServletBinder;

@Singleton
public class ServletBinderImpl
	implements ServletBinder
{
	private final Injector injector;
	private final Lock lock;
	private volatile FilterEntry[] filters;
	private volatile ServletEntry[] servlets;

	@Inject
	public ServletBinderImpl(Injector injector)
	{
		this.injector = injector;

		lock = new ReentrantLock();
		filters = new FilterEntry[0];
		servlets = new ServletEntry[0];
	}

	@Override
	public FilterBuilder filterRegex(String path)
	{
		return new FilterBuilderImpl(path).regex();
	}

	@Override
	public FilterBuilder filter(String path)
	{
		return new FilterBuilderImpl(path);
	}

	@Override
	public ServletBuilder serve(String path)
	{
		return new ServletBuilderImpl(path);
	}

	@Override
	public ServletBuilder serveRegex(String path)
	{
		return new ServletBuilderImpl(path).regex();
	}

	public FilterEntry[] getFilters()
	{
		return filters;
	}

	public ServletEntry[] getServlets()
	{
		return servlets;
	}

	private Matcher createMatcher(boolean regex, String path)
	{
		if(regex)
		{
			return new RegexMatcher(path);
		}
		else if("/*".equals(path))
		{
			return AnyMatcher.INSTANCE;
		}
		else
		{
			return new NormalMatcher(path);
		}
	}

	private class FilterBuilderImpl
		implements FilterBuilder
	{
		private final Map<String, String> params;
		private final String path;

		private boolean regex;

		public FilterBuilderImpl(String path)
		{
			this.path = path;

			params = new HashMap<String, String>();
		}

		public FilterBuilder regex()
		{
			this.regex = true;

			return this;
		}

		@Override
		public void with(Filter instance)
		{
			FilterEntry e = new FilterEntry(
				path,
				createMatcher(regex, path),
				instance,
				params
			);

			lock.lock();
			try
			{
				FilterEntry[] result = new FilterEntry[filters.length + 1];
				System.arraycopy(filters, 0, result, 0, filters.length);
				result[filters.length] = e;
				filters = result;
			}
			finally
			{
				lock.unlock();
			}
		}

		@Override
		public void with(Provider<? extends Filter> provider)
		{
			with(provider.get());
		}

		@Override
		public void with(Class<? extends Filter> filter)
		{
			with(injector.getInstance(filter));
		}

		@Override
		public FilterBuilder param(String key, String value)
		{
			params.put(key, value);

			return this;
		}

		@Override
		public FilterBuilder params(Map<String, String> params)
		{
			params.putAll(params);

			return this;
		}
	}

	private class ServletBuilderImpl
		implements ServletBuilder
	{
		private final Map<String, String> params;
		private final String path;

		private boolean regex;

		public ServletBuilderImpl(String path)
		{
			this.path = path;

			params = new HashMap<String, String>();
		}

		public ServletBuilder regex()
		{
			this.regex = true;

			return this;
		}

		@Override
		public void with(Provider<? extends Servlet> provider)
		{
			with(provider.get());
		}

		@Override
		public void with(Servlet instance)
		{
			ServletEntry e = new ServletEntry(
				path,
				createMatcher(regex, path),
				instance,
				params
			);

			lock.lock();
			try
			{
				ServletEntry[] result = new ServletEntry[servlets.length + 1];
				System.arraycopy(servlets, 0, result, 0, servlets.length);
				result[servlets.length] = e;
				servlets = result;
			}
			finally
			{
				lock.unlock();
			}
		}

		@Override
		public void with(Class<? extends Servlet> servlet)
		{
			with(injector.getInstance(servlet));
		}

		@Override
		public ServletBuilder param(String key, String value)
		{
			params.put(key, value);

			return this;
		}

		@Override
		public ServletBuilder params(Map<String, String> params)
		{
			params.putAll(params);

			return this;
		}
	}
}
