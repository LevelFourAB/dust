package se.l4.dust.jaxrs.spi;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

import se.l4.dust.jaxrs.PageProvider;
import se.l4.dust.jaxrs.ServletBinder;

import com.google.inject.Injector;

/**
 * Configuration abstraction for different JAX-RS implementations.
 * 
 * @author andreas
 *
 */
public interface Configuration
{
	void addMessageBodyWriter(MessageBodyWriter<?> writer);
	
	void addMessageBodyReader(MessageBodyReader<?> reader);
	
	void addPage(PageProvider factory);
	
	void removePage(PageProvider factory);
	
	void setupContext(ServletContext ctx, Injector injector);
	
	Class<? extends HttpServlet> getRootServlet();

	void setupFilter(ServletContext ctx, Injector injector, ServletBinder binder);
}
