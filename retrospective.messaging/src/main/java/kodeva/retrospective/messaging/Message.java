package kodeva.retrospective.messaging;

/**
 * Message object.
 * 
 * @author Stepan Hrbacek
 */
public class Message {
	private Message() {
	}

	/**
	 * Construction of message filters.
	 */
	public final class Builder {
		public Message build() {
			return new Message();
		}
	}
}
