package se.l4.dust.api.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import se.l4.dust.api.DocumentLinker;
import se.l4.dust.api.template.TemplateRenderer;

/**
 * Indicate that this method on a component should be run before the component
 * is rendered. The arguments of the method will be injected. To accept 
 * parameters {@link TemplateParam} should be used. Other arguments will by
 * default be resolved via Guice. The behavior of this method invocation can
 * be overridden by the user of {@link TemplateRenderer}.
 * 
 * <p>
 * Examples:
 * 
 * <ul>
 *	<li>
 *		<code>@{literal void prepare(@TemplateParam("name") String name)}</code><br>
 * 		Will receive the parameter {@code name} as defined by the template
 * 		calling the component
 *	</li>
 *	<li>
 *		<code>@{literal void prepare(DocumentLinker linker)}</code><br>
 *		Will receive an instance of {@link DocumentLinker}.
 *	</li>
 * </ul>
 * 
 * <p>
 * The result of the method will be used a the data when rendering the 
 * template of the component. If no result is returned the component instance
 * will be used instead.
 * 
 * @author Andreas Holstenson
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface PrepareRender
{
}
