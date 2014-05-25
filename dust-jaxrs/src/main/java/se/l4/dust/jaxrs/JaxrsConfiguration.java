package se.l4.dust.jaxrs;

import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.ParamConverterProvider;

/**
 * JAX-RS configuration, to allow for manual adding of pages and providers.
 * 
 * @author Andreas Holstenson
 *
 */
public interface JaxrsConfiguration
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
}
