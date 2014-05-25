package se.l4.dust.api.template;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import se.l4.dust.api.ContextScoped;

/**
 * Indicate that a method should be run after a component has been rendered.
 * This is useful to perform clean up if needed.
 * 
 * <p>
 * This is usually needed when the component is {@link ContextScoped}.
 * 
 * @author Andreas Holstenson
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface AfterRender
{

}
