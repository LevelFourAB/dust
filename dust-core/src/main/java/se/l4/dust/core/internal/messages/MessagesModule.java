package se.l4.dust.core.internal.messages;

import se.l4.crayon.CrayonModule;
import se.l4.crayon.annotation.Contribution;
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

	@Contribution(name="dust-messages")
	public void contributeDefaultMessageSources(Messages manager, PropertyMessagesSource properties)
	{
		manager.addSource(properties);
	}
	
	@Contribution
	public void bindNamespace(Namespaces manager)
	{
		manager.bind("dust:messages").add();
	}
	
	@TemplateContribution
	public void bindExpressionSource(Expressions expressions,
			MessageExpressionSource source)
	{
		expressions.addSource("dust:messages", source);
	}
}
