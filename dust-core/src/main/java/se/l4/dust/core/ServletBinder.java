package se.l4.dust.core;

import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.Servlet;

import com.google.inject.Scopes;

public interface ServletBinder
{
	ServletBuilder serve(String path);
	
	ServletBuilder serveRegex(String path);
	
	FilterBuilder filter(String path);
	
	FilterBuilder filterRegex(String path);
	
	/**
	 * Builder for filters.
	 * 
	 * @author Andreas Holstenson
	 *
	 */
	interface FilterBuilder
	{
		FilterBuilder params(Map<String, String> params);
		
		FilterBuilder param(String key, String value);

		/**
		 * Define the filter to bind. This method must be called last as it 
		 * will register the definition. The filter should be scoped as
		 * {@link Scopes#SINGLETON}.
		 * 
		 * @param type
		 */
		void with(Class<? extends Filter> type);
	}
	
	interface ServletBuilder
	{
		ServletBuilder params(Map<String, String> params);
		
		ServletBuilder param(String key, String value);

		/**
		 * Define the servlet to bind. This method must be called last as it 
		 * will register the definition. The servlet should be scoped as
		 * {@link Scopes#SINGLETON}.
		 * 
		 * @param type
		 */
		void with(Class<? extends Servlet> type);
	}
}
