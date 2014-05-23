package se.l4.dust.jaxrs.spi;

import javax.servlet.ServletContext;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.ParamConverterProvider;

import se.l4.dust.jaxrs.ServletBinder;

import com.google.inject.Injector;
import com.google.inject.Provider;

/**
 * Configuration abstraction for different JAX-RS implementations.
 * 
 * @author andreas
 *
 */
public interface Configuration
{
	/**
	 * Add a {@link MessageBodyWriter} to the current JAX-RS configuration.
	 * 
	 * @param writer
	 */
	void addMessageBodyWriter(MessageBodyWriter<?> writer);
	
	/**
	 * Add a {@link MessageBodyReader} to the current JAX-RS configuration.
	 * 
	 * @param writer
	 */
	void addMessageBodyReader(MessageBodyReader<?> reader);
	
	/**
	 * Add a {@link ExceptionMapper} to the current JAX-RS configuration.
	 * 
	 * @param mapper
	 */
	void addExceptionMapper(ExceptionMapper<?> mapper);
	
	/**
	 * Add a {@link ParamConverterProvider} to the current JAX-RS
	 * configuration.
	 * 
	 * @param provider
	 */
	void addParamConverterProvider(ParamConverterProvider provider);
	
	/**
	 * Add a page to to the configuration.
	 * 
	 * @param factory
	 */
	void addPage(Class<?> typeAnnotatedWithPath);
	
	/**
	 * Setup the servlet context.
	 * 
	 * @param ctx
	 * @param injector
	 */
	void setupContext(ServletContext ctx, Injector injector);
	
	/**
	 * Perform setup and bind any servlets and filters required for normal
	 * operation.
	 * 
	 * @param ctx
	 * @param injector
	 * @param binder
	 */
	void setupFilter(ServletContext ctx, Injector injector, ServletBinder binder);
}
