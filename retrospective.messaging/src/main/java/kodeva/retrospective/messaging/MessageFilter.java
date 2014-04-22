package kodeva.retrospective.messaging;

/**
 * Filter for message subscribing.
 * 
 * @author Stepan Hrbacek
 */
public class MessageFilter {
	private MessageFilter() {
	}

	/**
	 * Construction of message filters.
	 */
	public final class Builder {
		public MessageFilter build() {
			return new MessageFilter();
		}
	}
}
