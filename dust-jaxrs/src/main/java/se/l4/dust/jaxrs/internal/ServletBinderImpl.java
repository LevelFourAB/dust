package se.l4.dust.jaxrs.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.servlet.Filter;
import javax.servlet.Servlet;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

import se.l4.dust.jaxrs.ServletBinder;
import se.l4.dust.jaxrs.internal.routing.FilterEntry;
import se.l4.dust.jaxrs.internal.routing.NormalMatcher;
import se.l4.dust.jaxrs.internal.routing.RegexMatcher;
import se.l4.dust.jaxrs.internal.routing.ServletEntry;

@Singleton
public class ServletBinderImpl
	implements ServletBinder
{
	private final Injector injector;
	private final List<FilterEntry> filters;
	private final List<ServletEntry> servlets;
	
	@Inject
	public ServletBinderImpl(Injector injector)
	{
		this.injector = injector;
		
		filters = new CopyOnWriteArrayList<FilterEntry>();
		servlets = new CopyOnWriteArrayList<ServletEntry>();
	}
	
	public FilterBuilder filterRegex(String path)
	{
		return new FilterBuilderImpl(path).regex();
	}
	
	public FilterBuilder filter(String path)
	{
		return new FilterBuilderImpl(path);
	}

	public ServletBuilder serve(String path)
	{
		return new ServletBuilderImpl(path);
	}
	
	public ServletBuilder serveRegex(String path)
	{
		return new ServletBuilderImpl(path).regex();
	}
	
	public List<FilterEntry> getFilters()
	{
		return filters;
	}
	
	public List<ServletEntry> getServlets()
	{
		return servlets;
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
		
		public void with(Class<? extends Filter> filter)
		{
			Filter instance = injector.getInstance(filter);
			filters.add(new FilterEntry(
				path, 
				regex ? new RegexMatcher(path) : new NormalMatcher(path), 
				instance, 
				params
			));
		}
		
		public FilterBuilder param(String key, String value)
		{
			params.put(key, value);
			
			return this;
		}
		
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
		
		public void with(Class<? extends Servlet> filter)
		{
			Servlet instance = injector.getInstance(filter);
			servlets.add(new ServletEntry(
				path, 
				regex ? new RegexMatcher(path) : new NormalMatcher(path), 
				instance, 
				params
			));
		}
		
		public ServletBuilder param(String key, String value)
		{
			params.put(key, value);
			
			return this;
		}
		
		public ServletBuilder params(Map<String, String> params)
		{
			params.putAll(params);
			
			return this;
		}
	}
}
