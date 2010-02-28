package se.l4.dust.api.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicate that this methods result should be rendered with a template.
 * The template is resolved by looking in the package of {@link #value()} for
 * a resource named as the class ending with {@code .xml}. Example: The class
 * {@code TestService} will have the default template {@code TestService.xml}.
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
@Inherited
public @interface Template
{
	/**
	 * Class used for resolving the template.
	 * 
	 * @return
	 */
	Class<?> value() default Object.class;
	
	/**
	 * The name of the template, leave empty for default.
	 * 
	 * @return
	 */
	String name() default "";
}
