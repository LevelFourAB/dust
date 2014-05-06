package se.l4.dust.core.internal.messages;

import se.l4.crayon.CrayonModule;
import se.l4.crayon.annotation.Contribution;
import se.l4.dust.api.annotation.TemplateContribution;
import se.l4.dust.api.expression.Expressions;
import se.l4.dust.api.messages.MessageManager;
import se.l4.dust.core.internal.expression.CommonSource;
import se.l4.dust.core.internal.expression.VarPropertySource;

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
		bind(MessageManager.class).to(MessageMangerImpl.class);
	}

	@Contribution(name="dust-messages")
	public void contributeDefaultMessageSources(MessageManager manager, PropertyMessagesSource properties)
	{
		manager.addSource(properties);
	}
	
	@TemplateContribution
	public void bindExpressionSource(Expressions expressions,
			MessageExpressionSource source)
	{
		expressions.addSource("dust:messages", source);
	}
}
