package se.l4.dust.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import se.l4.dust.api.NamespaceManager;

import com.google.inject.BindingAnnotation;

/**
 * Annotation for binding a namespace in {@link NamespaceManager}.
 * 
 * <p>
 * Example:
 * <pre>
 * {@literal @NamespaceBinding}
 * public void bindNamespace(NamespaceManager manager) {
 * 	manager.bind("namespaceurl")
 * 		.setPackage(getClass())
 * 		.setPrefix("prefix")
 * 		.add();
 * }
 * </pre>
 * 
 * @author Andreas Holstenson
 *
 */
@BindingAnnotation
@Target({ ElementType.PARAMETER, ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface NamespaceBinding
{
	/**
	 * Define the name of the binding, if needed for ordering.
	 * 
	 * @return
	 */
	String name() default "";
}
