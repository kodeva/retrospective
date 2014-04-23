package kodeva.retrospective.messaging;

import static org.junit.Assert.*;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

public class MessageBrokerTest {

	@Test
	public void testInvalidInput() {
		final MessageBroker mb = new MessageBroker();
		final MessageFilter filterAll = new MessageFilter.Builder().build();
		final MessageProcessor processor = new MessageProcessor() {
			@Override
			public void process(Message message) {
			}
		};
		
		try {
			mb.subscribe(null, null);
			fail();
		} catch (NullPointerException e) {
		}
		try {
			mb.subscribe(filterAll, null);
			fail();
		} catch (NullPointerException e) {
		}
		try {
			mb.subscribe(null, processor);
			fail();
		} catch (NullPointerException e) {
		}

		try {
			mb.sendMessage(null);
			fail();
		} catch (NullPointerException e) {
		}
	}
	
	@Test
	public void testSimpleFilters() {
		final MessageBroker mb = new MessageBroker();

		final MessageFilter filterAll = new MessageFilter.Builder().build();
		final AtomicInteger counterAll = new AtomicInteger(0);
		final MessageProcessor processorAll = new MessageProcessor() {
			@Override
			public void process(Message message) {
				counterAll.incrementAndGet();
			}
		};
		mb.subscribe(filterAll, processorAll);

		final String sender2 = "sender-2";
		final MessageFilter filterSender2 = new MessageFilter.Builder().sender(sender2).build();
		final AtomicInteger counterSender2 = new AtomicInteger(0);
		final MessageProcessor processorSender2 = new MessageProcessor() {
			@Override
			public void process(Message message) {
				counterSender2.incrementAndGet();
			}
		};
		mb.subscribe(filterSender2, processorSender2);

		final Message messageFromSender1 = new Message.Builder().sender("sender-1").build();
		final Message messageFromSender2 = new Message.Builder().sender(sender2).build();

		final int count = 100;
		for (int i = 0; i < count; i++) {
			mb.sendMessage(messageFromSender1);
			mb.sendMessage(messageFromSender2);
		}
		assertEquals(count * 2, counterAll.get());
		assertEquals(count, counterSender2.get());
	}
}
