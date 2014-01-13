package se.l4.dust.jaxrs.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.inject.BindingAnnotation;

/**
 * Marker for contributions that contribute filters.
 * 
 * @author Andreas Holstenson
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.PARAMETER })
@BindingAnnotation
@Documented
public @interface Filter
{
	String name() default "";
}
