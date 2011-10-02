package se.l4.dust.jaxrs.spi;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Information about the current request context.
 * 
 * @author Andreas Holstenson
 *
 */
public interface RequestContext
{
	/**
	 * Get the current servlet request.
	 * 
	 * @return
	 */
	HttpServletRequest getHttpServletRequest();
	
	/**
	 * Get the current servlet response.
	 * 
	 * @return
	 */
	HttpServletResponse getHttpServletResponse();
	
	/**
	 * Get the current session.
	 * 
	 * @return
	 */
	HttpSession getHttpSession();
	
	/**
	 * Get the current servlet context.
	 * 
	 * @return
	 */
	ServletContext getServletContext();
}
