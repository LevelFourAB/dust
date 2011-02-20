package se.l4.dust.jaxrs;

import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.Servlet;


import com.google.inject.Scopes;

/**
 * Binder for filters and servlet. When used with {@link DustFilter} this 
 * allows configuring filters and servlets with Java code and Guice injection
 * during creation.
 * 
 * <p>
 * Binding a filter:
 * <pre>
 * binder.filter("/*").with(FilterImpl.class);
 * binder.filter("/*").param("param.key", "value").with(FilterImpl.class);
 * binder.filterRegex("/[0-9]+/.+/xml").with(FilterImpl.class)
 * </pre>
 * 
 * <p>
 * Binding a servlet:
 * <pre>
 * binder.serve("/*").with(ServletImpl.class);
 * binder.serve("/*").param("param.key", "value").with(ServletImpl.class);
 * binder.serveRegex("/[0-9]+/.+/xml").with(ServletImpl.class)
 * </pre>
 * 
 * @author Andreas Holstenson
 *
 */
public interface ServletBinder
{
	/**
	 * Start binding of a servlet on a certain path. The path will match using
	 * the same rules as a definition in @{code web.xml}. Be sure to call
	 * {@link ServletBuilder#with(Class)} to register the servlet.
	 * 
	 * <p>
	 * Parameters can be specified before calling {@code with}.
	 * 
	 * @param path
	 * @return
	 */
	ServletBuilder serve(String path);
	
	/**
	 * Start binding of a servlet on a certain path. The path will be treated
	 * as a regular expression and the servlet will be used if the expression
	 * matches the path of a request.
	 * 
	 * @see #serve(String)
	 * @param path
	 * @return
	 */
	ServletBuilder serveRegex(String path);

	/**
	 * Start binding of a filter on a certain path. The path will match using
	 * the same rules as a definition in @{code web.xml}. Be sure to call
	 * {@link FilterBuilder#with(Class)} to register the filter.
	 * 
	 * @param path
	 * @return
	 */
	FilterBuilder filter(String path);
	
	/**
	 * Start binding of a filter on a certain path. The path will be treated
	 * as a regular expression and the filter will be used if the expression
	 * matches the path of a request.
	 * 
	 * @see #filter(String)
	 * @param path
	 * @return
	 */
	FilterBuilder filterRegex(String path);
	
	/**
	 * Builder for filters.
	 * 
	 * @author Andreas Holstenson
	 *
	 */
	interface FilterBuilder
	{
		/**
		 * Define several parameters to be given to the filter.
		 * 
		 * @param params
		 * @return
		 */
		FilterBuilder params(Map<String, String> params);
		
		/**
		 * Define a parameter to the filter.
		 * 
		 * @param key
		 * @param value
		 * @return
		 */
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
	
	/**
	 * Builder for servlets.
	 * 
	 * @author Andreas Holstenson
	 *
	 */
	interface ServletBuilder
	{
		/**
		 * Define several parameters to be given to the servlet.
		 * 
		 * @param params
		 * @return
		 */
		ServletBuilder params(Map<String, String> params);
		
		/**
		 * Define a parameter to the servlet.
		 * 
		 * @param key
		 * @param value
		 * @return
		 */
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
