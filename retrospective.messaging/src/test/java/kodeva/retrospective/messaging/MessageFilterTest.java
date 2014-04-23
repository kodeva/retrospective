package kodeva.retrospective.messaging;

import static org.junit.Assert.*;

import java.util.AbstractMap;

import org.junit.Test;

public class MessageFilterTest {

	@Test
	public void testKeyValueFilter() {
		MessageFilter.Builder filterBldr = new MessageFilter.Builder();
		try {
			filterBldr.key(null).build();
			fail();
		} catch (NullPointerException e) {
		}
		try {
			filterBldr.key("").build();
			fail();
		} catch (IllegalArgumentException e) {
		}
		try {
			filterBldr.key("A").value(null).build();
			fail();
		} catch (NullPointerException e) {
		}
		try {
			filterBldr.sender(null).build();
			fail();
		} catch (NullPointerException e) {
		}
		try {
			filterBldr.sender("").build();
			fail();
		} catch (IllegalArgumentException e) {
		}

		final Message msgKeyAValueBlobSenderAlice = new Message.Builder().entry(new AbstractMap.SimpleImmutableEntry<>("A", "Blob")).sender("Alice").build();
		final Message msgKeyAValueAliceSenderBlob = new Message.Builder().entry(new AbstractMap.SimpleImmutableEntry<>("A", "Alice")).sender("Blob").build();
		final Message msgKeyBValueBlobSenderAlice = new Message.Builder().entry(new AbstractMap.SimpleImmutableEntry<>("B", "Blob")).sender("Alice").build();
		final Message msgKeyAEmptySenderAlice = new Message.Builder().entry(new AbstractMap.SimpleImmutableEntry<>("A", "")).sender("Alice").build();

		final MessageFilter filterAll = new MessageFilter.Builder().build();
		assertTrue(filterAll.matches(msgKeyAValueBlobSenderAlice));
		assertTrue(filterAll.matches(msgKeyAValueAliceSenderBlob));
		assertTrue(filterAll.matches(msgKeyBValueBlobSenderAlice));
		assertTrue(filterAll.matches(msgKeyAEmptySenderAlice));

		final MessageFilter filterAllKeyAValues = new MessageFilter.Builder().key("A").build();
		assertTrue(filterAllKeyAValues.matches(msgKeyAValueBlobSenderAlice));
		assertTrue(filterAllKeyAValues.matches(msgKeyAValueAliceSenderBlob));
		assertFalse(filterAllKeyAValues.matches(msgKeyBValueBlobSenderAlice));
		assertTrue(filterAllKeyAValues.matches(msgKeyAEmptySenderAlice));

		final MessageFilter filterEmptyKeyAValue = new MessageFilter.Builder().key("A").value("").build();
		assertFalse(filterEmptyKeyAValue.matches(msgKeyAValueBlobSenderAlice));
		assertFalse(filterEmptyKeyAValue.matches(msgKeyAValueAliceSenderBlob));
		assertFalse(filterEmptyKeyAValue.matches(msgKeyBValueBlobSenderAlice));
		assertTrue(filterEmptyKeyAValue.matches(msgKeyAEmptySenderAlice));

		final MessageFilter filterKeyAValueBlob = new MessageFilter.Builder().key("A").value("Blob").build();
		assertTrue(filterKeyAValueBlob.matches(msgKeyAValueBlobSenderAlice));
		assertFalse(filterKeyAValueBlob.matches(msgKeyAValueAliceSenderBlob));
		assertFalse(filterKeyAValueBlob.matches(msgKeyBValueBlobSenderAlice));
		assertFalse(filterKeyAValueBlob.matches(msgKeyAEmptySenderAlice));

		final MessageFilter filterSenderAlice = new MessageFilter.Builder().sender("Alice").build();
		assertTrue(filterSenderAlice.matches(msgKeyAValueBlobSenderAlice));
		assertFalse(filterSenderAlice.matches(msgKeyAValueAliceSenderBlob));
		assertTrue(filterSenderAlice.matches(msgKeyBValueBlobSenderAlice));
		assertTrue(filterSenderAlice.matches(msgKeyAEmptySenderAlice));
	}
	
	@Test
	public void testComposedFilter() {
		final Message msgKeyAValueBlobSenderAlice = new Message.Builder().entry(new AbstractMap.SimpleImmutableEntry<>("A", "Blob")).sender("Alice").build();
		final Message msgKeyAValueAliceSenderBlob = new Message.Builder().entry(new AbstractMap.SimpleImmutableEntry<>("A", "Alice")).sender("Blob").build();
		final Message msgKeyAValueEmptySenderAlice = new Message.Builder().entry(new AbstractMap.SimpleImmutableEntry<>("A", "")).sender("Alice").build();

		final MessageFilter filterKeyAValueBlob = new MessageFilter.Builder().key("A").value("Blob").build();
		final MessageFilter filterSenderAlice = new MessageFilter.Builder().sender("Alice").build();

		assertFalse(filterKeyAValueBlob.not().matches(msgKeyAValueBlobSenderAlice));
		assertTrue(filterSenderAlice.not().matches(msgKeyAValueAliceSenderBlob));
		
		assertFalse(filterKeyAValueBlob.or(filterSenderAlice).matches(msgKeyAValueAliceSenderBlob));
		assertTrue(filterKeyAValueBlob.or(filterSenderAlice).matches(msgKeyAValueEmptySenderAlice));
		assertTrue(filterSenderAlice.or(filterKeyAValueBlob).matches(msgKeyAValueEmptySenderAlice));
		assertTrue(filterSenderAlice.or(filterKeyAValueBlob).matches(msgKeyAValueBlobSenderAlice));
		
		assertFalse(filterKeyAValueBlob.and(filterSenderAlice).matches(msgKeyAValueAliceSenderBlob));
		assertFalse(filterKeyAValueBlob.and(filterSenderAlice).matches(msgKeyAValueEmptySenderAlice));
		assertFalse(filterSenderAlice.and(filterKeyAValueBlob).matches(msgKeyAValueEmptySenderAlice));
		assertTrue(filterSenderAlice.and(filterKeyAValueBlob).matches(msgKeyAValueBlobSenderAlice));
	}
}
