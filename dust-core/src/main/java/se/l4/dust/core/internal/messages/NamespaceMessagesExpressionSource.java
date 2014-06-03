package se.l4.dust.core.internal.messages;

import se.l4.dust.api.Namespace;
import se.l4.dust.api.expression.ExpressionEncounter;
import se.l4.dust.api.messages.Messages;
import se.l4.dust.api.resource.NamespaceLocation;
import se.l4.dust.api.resource.ResourceLocation;

import com.google.inject.Stage;

public class NamespaceMessagesExpressionSource
	extends MessageExpressionSource
{
	private final NamespaceLocation location;

	public NamespaceMessagesExpressionSource(Stage stage,
			Messages messageManager,
			Namespace namespace,
			String name)
	{
		super(stage, messageManager);
		location = new NamespaceLocation(namespace, name);
	}

	@Override
	protected ResourceLocation getLocation(ExpressionEncounter encounter)
	{
		return location;
	}
}
