package kodeva.retrospective.messaging;

import java.util.AbstractMap;

import kodeva.retrospective.messaging.Message.MessageEntryKey;

/**
 * Filter for message subscribing.
 * 
 * @author Stepan Hrbacek
 */
public abstract class MessageFilter {
	/**
	 * Creates negation.
	 * @return Negation of this filter - NOT(this)
	 */
	public final MessageFilter not() {
		final MessageFilter thisFilter = this;
		return new MessageFilter() {
			@Override
			boolean matches(Message message) {
				return ! thisFilter.matches(message);
			}
		};
	}
	
	/**
	 * Creates conjunction.
	 * @param secondFilter
	 *  second filter for conjunction
	 * @return Conjunction of this and secondFilter - this AND secondFilter
	 */
	public final MessageFilter and(final MessageFilter secondFilter) {
		final MessageFilter firstFilter = this;
		return new MessageFilter() {
			@Override
			boolean matches(Message message) {
				return firstFilter.matches(message) && secondFilter.matches(message);
			}
		};
	}
	
	/**
	 * Creates disjunction.
	 * @param secondFilter
	 *  second filter for disjunction
	 * @return Disjunction of this and secondFilter - this OR secondFilter
	 */
	public final MessageFilter or(final MessageFilter secondFilter) {
		final MessageFilter firstFilter = this;
		return new MessageFilter() {
			@Override
			boolean matches(Message message) {
				return firstFilter.matches(message) || secondFilter.matches(message);
			}
		};
	}
	
	/**
	 * @param message
	 *  message to be matched with this filter
	 * @return <code>true</code> (the message matches this filter) / <code>false</code> (otherwise)
	 */
	abstract boolean matches(Message message);

	/**
	 * Construction of simple message filters. Simple message filters can be composed into complex ones by the MessageFilter class.
	 * Without specifying any filtering, the builder produces filter that matches all messages.
	 */
	public static final class Builder {
		private String key, value;
		
		/**
		 * Specifies key for filtering messages containing [key, value] pair.
		 * @param key
		 *  key id - not null, not empty
		 * @return
		 * 	this instance
		 */
		public Builder key(String key) {
			if (key.trim().length() == 0) {
				throw new IllegalArgumentException("Empty keys not allowed");
			}
			this.key = key;
			return this;
		}
		
		/**
		 * Specifies value for filtering messages containing [key, value] pair. Not specifying the value means any value.
		 * @param value
		 *  value, not null
		 * @return
		 * 	this instance
		 */
		public Builder value(String value) {
			if (value == null) {
				throw new NullPointerException("Null value not allowed");
			}
			this.value = value;
			return this;
		}
		
		/**
		 * Specifies sender for filtering messages.
		 * @param sender
		 *  message sender
		 * @return
		 * 	this instance
		 */
		public Builder sender(String sender) {
			if (sender.trim().length() == 0) {
				throw new IllegalArgumentException("Empty Sender not allowed");
			}
			key(MessageEntryKey.Sender.toString());
			value(sender);
			return this;
		}

		/**
		 * @return Builds simple filter instance.
		 */
		public MessageFilter build() {
			if (key == null) {
				return new AllFilter();
			} else {
				return new KeyValueMessageFilter(key, value);
			}
		}
	}

	private static final class AllFilter extends MessageFilter {
		@Override
		boolean matches(Message message) {
			return true;
		}
	}

	private static final class KeyValueMessageFilter extends MessageFilter {
		private String key, value;
		
		private KeyValueMessageFilter(String key, String value) {
			this.key = key;
			this.value = value;
		}
		
		@Override
		boolean matches(Message message) {
			if (value == null) {
				return message.containsKey(key);
			}
			return message.containsEntry(new AbstractMap.SimpleImmutableEntry<>(key, value));
		}
	}
}
