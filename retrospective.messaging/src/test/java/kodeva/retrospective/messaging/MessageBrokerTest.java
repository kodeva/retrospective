package kodeva.retrospective.messaging;

import static org.junit.Assert.*;

import java.util.concurrent.CountDownLatch;
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
			mb.unsubscribe(null, null);
			fail();
		} catch (NullPointerException e) {
		}
		try {
			mb.unsubscribe(filterAll, null);
			fail();
		} catch (NullPointerException e) {
		}
		try {
			mb.unsubscribe(null, processor);
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
	public void testSimpleFiltersSingleThreaded() {
		testSimpleFilters(new MessageBroker());
	}
	
	@Test
	public void testSimpleFiltersMultiThreaded() {
		final MessageBroker mb = new MessageBroker();
		final int threadsCount = 5;
		final CountDownLatch counter = new CountDownLatch(threadsCount);
		for (int i = 1; i <= threadsCount; i++) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						testSimpleFilters(mb);
					} finally {
						counter.countDown();
					}
				}
			}).start();
		}

		try {
			counter.await();
		} catch (InterruptedException e) {
		}
	}

	private static void testSimpleFilters(final MessageBroker mb) {
		final MessageFilter filterAll = new MessageFilter.Builder().build();

		final MessageProcessor processorFailFirst = new MessageProcessor() {
			@Override
			public void process(Message message) {
				throw new RuntimeException("Just failing as first...");
			}
		};
		mb.subscribe(filterAll, processorFailFirst);
		
		final AtomicInteger counterAll = new AtomicInteger(0);
		final MessageProcessor processorAll = new MessageProcessor() {
			@Override
			public void process(Message message) {
				counterAll.incrementAndGet();
			}
		};
		mb.subscribe(filterAll, processorAll);

		final MessageProcessor processorFailLast = new MessageProcessor() {
			@Override
			public void process(Message message) {
				throw new RuntimeException("Just failing as last...");
			}
		};
		mb.subscribe(filterAll, processorFailLast);
		
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

		// Unsubscribe and test that no message was processed
		mb.unsubscribe(filterAll, processorFailFirst);
		mb.unsubscribe(filterAll, processorAll);
		mb.unsubscribe(filterAll, processorFailLast);
		mb.unsubscribe(filterSender2, processorSender2);
		
		final int counterAllCurrent = counterAll.get();
		final int counterSender2Current = counterSender2.get();
		mb.sendMessage(messageFromSender1);
		mb.sendMessage(messageFromSender2);
		assertEquals(counterAllCurrent, counterAll.get());
		assertEquals(counterSender2Current, counterSender2.get());
	}
}
