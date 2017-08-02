package se.l4.dust.core.internal.messages;

import com.google.inject.name.Named;

import se.l4.crayon.Contribution;
import se.l4.crayon.CrayonModule;
import se.l4.dust.api.NamespaceBinding;
import se.l4.dust.api.Namespaces;
import se.l4.dust.api.expression.Expressions;
import se.l4.dust.api.messages.Messages;
import se.l4.dust.api.template.TemplateContribution;

/**
 * Module for message support.
 *
 * @author Andreas Holstenson
 *
 */
public class MessagesModule
	extends CrayonModule
{

	@Override
	protected void configure()
	{
		bind(Messages.class).to(MessagesImpl.class);
	}

	@Contribution
	@Named("dust-messages")
	public void contributeDefaultMessageSources(Messages manager,
			PropertyMessagesSource properties,
			CustomMessagesSource messages)
	{
		manager.addSource(properties);
		manager.addSource(messages);
	}

	@NamespaceBinding
	public void bindNamespace(Namespaces manager)
	{
		manager.bind("dust:messages").manual().add();
	}

	@TemplateContribution
	public void bindExpressionSource(Expressions expressions,
			MessageExpressionSource source)
	{
		expressions.addSource("dust:messages", source);
	}
}
