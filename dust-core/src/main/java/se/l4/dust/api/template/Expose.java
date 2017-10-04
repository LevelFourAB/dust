package se.l4.dust.api.template;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Expose information from a class for use in expressions. This annotation
 * can be placed on private fields to make them usable in expression without
 * writing getters or setters.
 *
 * @author Andreas Holstenson
 *
 */
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Expose
{
}
