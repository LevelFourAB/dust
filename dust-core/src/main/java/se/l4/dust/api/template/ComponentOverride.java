package se.l4.dust.api.template;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Similar to {@link Component}, but this overrides a component in another
 * template. A component that is overridden is rendered with its own template,
 * but must extend the original component class and can not provide any
 * custom {@link PrepareRender}-methods or {@link TemplateParam}-setters.
 *
 * @author Andreas Holstenson
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface ComponentOverride
{
	/**
	 * Get the namespace that is being overridden.
	 *
	 * @return
	 */
	String namespace();

	/**
	 * Get the class of the component that is being overridden.
	 *
	 * @return
	 */
	Class<?> component();
}
