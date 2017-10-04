package se.l4.dust.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.spi.ConfigurationState;

import org.apache.bval.jsr303.ApacheValidationProvider;
import org.apache.bval.jsr303.ConfigurationImpl;
import org.apache.bval.jsr303.DefaultMessageInterpolator;
import org.apache.bval.jsr303.resolver.DefaultTraversableResolver;

import se.l4.crayon.CrayonModule;
import se.l4.dust.api.NamespaceBinding;
import se.l4.dust.api.Namespaces;
import se.l4.dust.api.expression.Expressions;
import se.l4.dust.api.template.Templates;

import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;

/**
 * Module for validation support. When this module is included it is possible
 * to use bean validation in templates.
 *
 * @author Andreas Holstenson
 *
 */
public class ValidationModule
	extends CrayonModule
{
	public static final String NAMESPACE = "dust:validation";
	public static final String CTX_FIELD = "dust:valdiation:field";
	public static final String TPL_FIELD = "field";
	public static final String CTX_VIOLATION = "dust:valdiation:violation";
	public static final String CTX_ERRORS = "dust:validation:errors";
	public static final String TPL_VIOLATIONS = "violations";

	@Override
	protected void configure()
	{
	}

	@Provides
	@Singleton
	public ConfigurationState provideConfigState(
			DefaultMessageInterpolator interpolator,
			DefaultTraversableResolver resolver,
			final Injector injector)
	{
		ApacheValidationProvider provider = new ApacheValidationProvider();

		ConfigurationImpl config = new ConfigurationImpl(null, provider);
		config.messageInterpolator(interpolator);
		config.traversableResolver(resolver);
		config.constraintValidatorFactory(new ConstraintValidatorFactory()
		{
			@Override
			public <T extends ConstraintValidator<?, ?>> T getInstance(Class<T> key)
			{
				return injector.getInstance(key);
			}
		});

		return config;
	}

	@Provides
	@Singleton
	public ValidatorFactory provideFactory(ConfigurationState state)
	{
		return new ApacheValidationProvider().buildValidatorFactory(state);
	}

	@Provides
	@Singleton
	public Validator provideValidator(ValidatorFactory factory)
	{
		return factory.getValidator();
	}

	@NamespaceBinding
	public void bindNamespace(Namespaces manager,
			Templates templates,
			ViolationsMixin mixin1,
			FieldMixin mixin2,
			Expressions exprs,
			ValidationExpressionSource source)
	{
		manager.bind(NAMESPACE)
			.add();

		templates.getNamespace(NAMESPACE)
			.addMixin(TPL_VIOLATIONS, mixin1)
			.addMixin(TPL_FIELD, mixin2);

		exprs.addSource(NAMESPACE, source);
	}
}
