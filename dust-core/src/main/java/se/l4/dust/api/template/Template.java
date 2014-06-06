package se.l4.dust.api.template;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicate that this methods result should be rendered with a template.
 * 
 * <p>
 * It is possible to override the name of the template by setting the value
 * of {@code #name()} to something else that an empty string.
 * 
 * @author Andreas Holstenson
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
public @interface Template
{
	/**
	 * The name of the template, leave empty for default.
	 * 
	 * @return
	 */
	String value() default "";
}
