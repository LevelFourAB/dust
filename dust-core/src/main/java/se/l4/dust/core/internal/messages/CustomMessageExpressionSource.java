package se.l4.dust.core.internal.messages;

import se.l4.dust.api.expression.ExpressionEncounter;
import se.l4.dust.api.messages.Messages;

import com.google.inject.Stage;

public class CustomMessageExpressionSource
	extends MessageExpressionSource
{
	private final String url;

	public CustomMessageExpressionSource(Stage stage, Messages messageManager, String url)
	{
		super(stage, messageManager);
		this.url = url;
	}

	@Override
	protected String getUrl(ExpressionEncounter encounter)
	{
		return url;
	}
}
