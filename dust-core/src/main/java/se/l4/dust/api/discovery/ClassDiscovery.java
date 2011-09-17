package se.l4.dust.api.discovery;

import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * Discovery interface for templates and components.
 * 
 * @author Andreas Holstenson
 *
 */
public interface ClassDiscovery
{
	/**
	 * Index classes in the current classpath.
	 */
	void index();
	
	/**
	 * Get classes annotated with the given annotation.
	 * 
	 * @param annotation
	 * @return
	 */
	Set<Class<?>> getAnnotatedWith(Class<? extends Annotation> annotation);
}
