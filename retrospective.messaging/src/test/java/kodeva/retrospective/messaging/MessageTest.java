package kodeva.retrospective.messaging;

import static org.junit.Assert.*;

import java.util.AbstractMap;
import java.util.Map;

import org.junit.Test;

public class MessageTest {

	@Test
	public void testBuilder() {
		try {
			new Message.Builder().build();
			fail();
		} catch (IllegalArgumentException e) {
		}
		
		try {
			new Message.Builder().entry(new AbstractMap.SimpleImmutableEntry<>("a", "b")).build();
			fail();
		} catch (IllegalArgumentException e) {
		}
		
		try {
			new Message.Builder().sender("Alice").entry(new AbstractMap.SimpleImmutableEntry<>((String) null, "b")).build();
			fail();
		} catch (NullPointerException e) {
		}
		
		try {
			new Message.Builder().sender("Alice").entry(new AbstractMap.SimpleImmutableEntry<>("", "b"));
			fail();
		} catch (IllegalArgumentException e) {
		}
		
		try {
			new Message.Builder().entry(new AbstractMap.SimpleImmutableEntry<>(Message.MessageEntryKey.Sender.toString(), "Sender"));
			fail();
		} catch (IllegalArgumentException e) {
		}

		final Map.Entry<String, String> entry1 = new AbstractMap.SimpleEntry<>("key1", "value1");
		final Map.Entry<String, String> entry2 = new AbstractMap.SimpleEntry<>("key2", "value2");
		final Message msg = new Message.Builder().sender("Alice").entry(entry1).entry(entry2).build();
		assertTrue(msg.containsKey(Message.MessageEntryKey.Sender.toString()));
		assertTrue(msg.containsKey("key1"));
		assertTrue(msg.containsKey("key2"));
		assertTrue(msg.containsEntry(new AbstractMap.SimpleImmutableEntry<>(Message.MessageEntryKey.Sender.toString(), "Alice")));
		assertTrue(msg.containsEntry(entry1));
		assertTrue(msg.containsEntry(entry2));
		
		// Test that Message makes its own copy of each entry
		entry1.setValue("value1-new");
		assertFalse(msg.containsEntry(entry1));
	}
}
