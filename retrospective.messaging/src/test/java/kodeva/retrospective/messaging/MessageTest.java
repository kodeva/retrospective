package kodeva.retrospective.messaging;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.hamcrest.Matchers;
import org.junit.Assert;
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
		final Map.Entry<String, String> entry3 = new AbstractMap.SimpleEntry<>("key3", "value3");
		final Map.Entry<String, String> entry4 = new AbstractMap.SimpleEntry<>("key4", "value4");
		final Message msg = new Message.Builder().sender("Alice").receiver("Bob").entry(entry1).entry(entry2)
				.entries(Arrays.asList(entry3, entry4)).build();
		assertTrue(msg.containsKey(Message.MessageEntryKey.Sender.toString()));
		assertTrue(msg.containsKey(Message.MessageEntryKey.Receiver.toString()));
		assertTrue(msg.containsKey("key1"));
		assertTrue(msg.containsKey("key2"));
		assertTrue(msg.containsKey("key3"));
		assertTrue(msg.containsKey("key4"));
		assertTrue(msg.containsEntry(new AbstractMap.SimpleImmutableEntry<>(Message.MessageEntryKey.Sender.toString(), "Alice")));
		assertTrue(msg.containsEntry(new AbstractMap.SimpleImmutableEntry<>(Message.MessageEntryKey.Receiver.toString(), "Bob")));
		assertTrue(msg.containsEntry(entry1));
		assertTrue(msg.containsEntry(entry2));
		assertTrue(msg.containsEntry(entry3));
		assertTrue(msg.containsEntry(entry4));
		
		// Test that Message makes its own copy of each entry
		entry1.setValue("value1-new");
		assertFalse(msg.containsEntry(entry1));

		final Message msgWithoutReceiver = new Message.Builder().sender("Alice").build();
		assertNull(msgWithoutReceiver.getReceiver());
		
		// Test conversion to string and creation from it
		final String msgStr = msg.toString();
		final Message msgNew = new Message.Builder().string(msgStr).build();
		Assert.assertEquals(msg.getSender(), msgNew.getSender());
		Assert.assertEquals(msgStr, msgNew.toString());
	}
	
	@Test
	public void testGetValues() {
		final Message msg = new Message.Builder().sender("Alice").receiver("Bob").
				entry(new AbstractMap.SimpleEntry<>("key1", "value1")).
				entry(new AbstractMap.SimpleEntry<>("key2", "value21")).
				entry(new AbstractMap.SimpleEntry<>("key2", "value22")).
				entry(new AbstractMap.SimpleEntry<>("key3", "value31")).
				entry(new AbstractMap.SimpleEntry<>("key3", "value32")).
				entry(new AbstractMap.SimpleEntry<>("key3", "value33")).build();
		Collection<String> empty = Collections.emptyList();
		Assert.assertThat(msg.getValues("key0"), Matchers.equalTo(empty));
		Assert.assertThat(msg.getValues("key1"), Matchers.equalTo((Collection<String>) Arrays.asList("value1")));
		Assert.assertThat(msg.getValues("key2"), Matchers.equalTo((Collection<String>) Arrays.asList("value21", "value22")));
		Assert.assertThat(msg.getValues("key3"), Matchers.equalTo((Collection<String>) Arrays.asList("value31", "value32", "value33")));
	}
	
	@Test
	public void testGetValue() {
		final Message msg = new Message.Builder().sender("Alice").receiver("Bob").
				entry(new AbstractMap.SimpleEntry<>("key1", "value1")).
				entry(new AbstractMap.SimpleEntry<>("key2", "value21")).
				entry(new AbstractMap.SimpleEntry<>("key2", "value22")).
				entry(new AbstractMap.SimpleEntry<>("key3", "value31")).
				entry(new AbstractMap.SimpleEntry<>("key3", "value32")).
				entry(new AbstractMap.SimpleEntry<>("key3", "value33")).build();
		Assert.assertEquals("Alice", msg.getSender());
		Assert.assertEquals("Bob", msg.getReceiver());
		Assert.assertNull(msg.getValue("key0"));
		Assert.assertEquals("value1", msg.getValue("key1"));
		Assert.assertTrue(Arrays.asList("value21", "value22").contains(msg.getValue("key2")));
		Assert.assertTrue(Arrays.asList("value31", "value32", "value33").contains(msg.getValue("key3")));
	}
}
