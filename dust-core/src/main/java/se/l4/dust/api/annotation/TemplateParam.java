package se.l4.dust.api.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicate that a parameter in the calling template should be given
 * to this component.
 * 
 * @author Andreas Holstenson
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER })
public @interface TemplateParam
{
	/**
	 * The name of the parameter.
	 * 
	 * @return
	 */
	String value();
}
