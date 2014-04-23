package kodeva.retrospective.messaging;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;


/**
 * Message object.
 * 
 * @author Stepan Hrbacek
 */
public class Message {
	private static final char SERIALIZATION_SEPARATOR_CHAR = ';';
	private static final String SERIALIZATION_SEPARATOR = Character.toString(SERIALIZATION_SEPARATOR_CHAR);
	private static final String SERIALIZATION_SEPARATOR_ESCAPED = SERIALIZATION_SEPARATOR + SERIALIZATION_SEPARATOR;
	
	private Collection<Map.Entry<String, String>> entries;
	
	private Message(Collection<Map.Entry<String, String>> entries) {
		this.entries = new ArrayList<>(entries);
	}
	
	/**
	 * Checks if the message contains an entry with given key and any value.
	 * @param key
	 *  key id
	 * @return
	 *  <code>true</code> (message contains given key) / <code>false</code> otherwise.
	 */
	public boolean containsKey(String key) {
		return containsKey(entries, key);
	}
	
	/**
	 * Checks if the message contains an entry with given key and value.
	 * @param pair
	 *  [key, value] pair
	 * @return
	 *  <code>true</code> (message contains given key) / <code>false</code> otherwise.
	 */
	public boolean containsEntry(Map.Entry<String, String> pair) {
		final Iterator<Map.Entry<String, String>> iter = entries.iterator();
		while (iter.hasNext()) {
			if (iter.next().equals(pair)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns collection of values of all entries with given key.
	 * @param key
	 *  key id
	 * @return
	 *  collection of values of all entries with given key / empty collection if no entries with given key exist.
	 */
	public Collection<String> getValues(String key) {
		final Collection<String> values = new ArrayList<>();
		final Iterator<Map.Entry<String, String>> iter = entries.iterator();
		while (iter.hasNext()) {
			final Map.Entry<String, String> entry = iter.next();
			if (entry.getKey().equals(key)) {
				values.add(entry.getValue());
			}
		}
		return values;
	}
	
	/**
	 * Returns message sender.
	 * @return
	 *  message sender identifier
	 */
	public String getSender() {
		return getValues(MessageEntryKey.Sender.toString()).iterator().next();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		final Iterator<Map.Entry<String, String>> iter = entries.iterator();
		while (iter.hasNext()) {
			final Map.Entry<String, String> entry = iter.next();
			sb.append(entry.getKey().replaceAll(SERIALIZATION_SEPARATOR, SERIALIZATION_SEPARATOR_ESCAPED));
			sb.append(SERIALIZATION_SEPARATOR);
			sb.append(entry.getValue().replaceAll(SERIALIZATION_SEPARATOR, SERIALIZATION_SEPARATOR_ESCAPED));
			sb.append(SERIALIZATION_SEPARATOR);
		}
		return sb.toString();
	}

	/**
	 * Key for well-known message entry types.
	 */
	static enum MessageEntryKey {
		Sender;
	}
	
	/**
	 * Checks if the provided collection contains an entry with given key and any value.
	 * @param entries
	 *  collection of entries
	 * @param key
	 *  key id
	 * @return
	 *  <code>true</code> (message contains given key) / <code>false</code> otherwise.
	 */
	private static boolean containsKey(Collection<Map.Entry<String, String>> entries, String key) {
		final Iterator<Map.Entry<String, String>> iter = entries.iterator();
		while (iter.hasNext()) {
			if (iter.next().getKey().equals(key)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Construction of message.
	 */
	public static final class Builder {
		private Collection<Map.Entry<String, String>> entries = new ArrayList<>();
		
		/**
		 * Specifies message entry.
		 * @param pair
		 *  message entry
		 * @return
		 * 	this instance
		 */
		public Builder entry(Map.Entry<String, String> pair) {
			if (pair.getKey().trim().length() == 0) {
				throw new IllegalArgumentException("Empty key is not allowed");
			}
			
			// Check if the specified key does not collide with the internal ones.
			try {
				MessageEntryKey.valueOf(pair.getKey());
			} catch (IllegalArgumentException e) {
				entries.add(new AbstractMap.SimpleImmutableEntry<String, String>(pair));
				return this;
			}
			throw new IllegalArgumentException(String.format("Key '%s' is not allowed for message entry", pair.getKey()));
		}

		/**
		 * Specifies message entries.
		 * @param pairs
		 *  message entries
		 * @return
		 * 	this instance
		 */
		public Builder entries(Collection<Map.Entry<String, String>> pairs) {
			for (Map.Entry<String, String> pair : pairs) {
				entry(pair);
			}
			return this;
		}
		
		/**
		 * Specifies serialized message.
		 * @param string
		 *  message serialized as string using Message.toString()
		 * @return
		 * 	this instance
		 */
		public Builder string(String string) {
			final StringBuilder escapedTokens = new StringBuilder(string);
			while (escapedTokens.length() > 0) {
				entries.add(new AbstractMap.SimpleImmutableEntry<String, String>(getNextUnescapedToken(escapedTokens), getNextUnescapedToken(escapedTokens)));
			}
			return this;
		}

		/**
		 * Get next unescaped token from escaped input and removes the token from the input.
		 * @param escapedTokens
		 *  serialized tokens using Message.toString()
		 * @return
		 *  unescaped token
		 */
		private static String getNextUnescapedToken(StringBuilder escapedTokens) {
			final StringBuilder unescapedToken = new StringBuilder();
			int endIndex;
			for (endIndex = 0; endIndex < escapedTokens.length(); endIndex++) {
				if (escapedTokens.charAt(endIndex) == SERIALIZATION_SEPARATOR_CHAR) {
					endIndex++;
					if ((endIndex == escapedTokens.length()) || (escapedTokens.charAt(endIndex) != SERIALIZATION_SEPARATOR_CHAR)) {
						break;
					}
				}
				unescapedToken.append(escapedTokens.charAt(endIndex));
			}
			escapedTokens.delete(0, endIndex);
			return unescapedToken.toString();
		}
		
		/**
		 * Specifies message sender.
		 * @param sender
		 *  message sender.
		 * @return
		 * 	this instance
		 */
		public Builder sender(String sender) {
			entries.add(new AbstractMap.SimpleImmutableEntry<String, String>(MessageEntryKey.Sender.toString(), sender));
			return this;
		}

		public Message build() {
			if (! containsKey(entries, MessageEntryKey.Sender.toString())) {
				throw new IllegalArgumentException("Sender is not specified");
			}
			return new Message(entries);
		}
	}
}
