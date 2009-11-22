package se.l4.dust.api.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import se.l4.dust.api.DocumentLinker;

/**
 * Indicate that this method on a component should be run before the component
 * is rendered. The arguments of the method will be injected in a fashion
 * similar to JAX-RS. Parameters annotated with {@link TemplateParam} will
 * receive parameters set in the calling template. Parameters that are not
 * handled by JAX-RS (usually not annotated) will be handled by Guice.
 * 
 * <p>
 * Examples:
 * 
 * <ul>
 * 	<li>
 * 		<code>@{literal void prepare(@QueryParam("q") String query)}</code><br>
 * 		Will receive the query parameter named {@code q}
 *	</li>
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
