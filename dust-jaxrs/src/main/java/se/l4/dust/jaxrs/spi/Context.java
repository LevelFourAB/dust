package se.l4.dust.jaxrs.spi;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Information about the current context.
 * 
 * @author andreas
 *
 */
public interface Context
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
