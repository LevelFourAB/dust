package se.l4.dust.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.inject.BindingAnnotation;

/**
 * Annotation for binding contributions to the template engine, such as
 * mixins and property sources.
 * 
 * <p>
 * Example:
 * <pre>
 * {@literal @TemplateContribution}
 * public void bindNamespace(TemplateManager manager) {
 * 	manager.getNamespace("namespaceurl")
 * 		.addMixin("mixin", mixinObject);
 * }
 * </pre>
 * 
 * @author Andreas Holstenson
 *
 */
@BindingAnnotation
@Target({ ElementType.PARAMETER, ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface TemplateContribution
{
	/**
	 * Define the name of the binding, if needed for ordering.
	 * 
	 * @return
	 */
	String name() default "";
}
