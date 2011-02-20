package se.l4.dust.jaxrs.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.inject.ScopeAnnotation;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@ScopeAnnotation
@Documented
public @interface ContextScoped
{

}
