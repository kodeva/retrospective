package kodeva.retrospective.model;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import kodeva.retrospective.messaging.Message;
import kodeva.retrospective.model.entity.AbstractEntity;
import kodeva.retrospective.model.entity.Card;
import kodeva.retrospective.model.entity.Card.Type;

/**
 * Adapter between entity object and message.
 */
public final class EntityMessageAdapter {
	private EntityMessageAdapter() {
	}
	
	/**
	 * Converts abstract entity data into message entries.
	 * @param entity
	 *  entity instance
	 * @return
	 *  message entries
	 */
	private static Collection<Map.Entry<String, String>> toMessageEntries(AbstractEntity entity) {
		final Collection<Map.Entry<String, String>> entries = new ArrayList<>();
		entries.add(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.ENTITY_ID, entity.getId()));
		return entries;
	}

	/**
	 * Sets entity data from message entries to builder.
	 * @param builder
	 *  abstract entity builder
	 * @param message
	 *  message containing entries with entity data
	 */
	private static void toAbstractEntity(AbstractEntity.Builder builder, Message message) {
		Collection<String> values = message.getValues(Constants.Messaging.Key.ENTITY_ID);
		if (! values.isEmpty()) {
			builder.id(values.iterator().next());
		}
	}
	
	/**
	 * Converts card data into message entries.
	 * @param card
	 *  card entity instance
	 * @return
	 *  message entries
	 */
	public static Collection<Map.Entry<String, String>> toMessageEntries(Card card) {
		final Collection<Map.Entry<String, String>> entries = toMessageEntries((AbstractEntity) card);
		entries.add(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.CARD_TYPE, card.getType().toString()));
		if (card.getFrontSideText() != null) {
			entries.add(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.CARD_FRONT_SIDE_TEXT, card.getFrontSideText()));
		}
		if (card.getBackSideText() != null) {
			entries.add(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.CARD_BACK_SIDE_TEXT, card.getBackSideText()));
		}
		return entries;
	}

	/**
	 * Creates card builder instance from message.
	 * @param message
	 *  message containing entries with card data
	 * @return
	 *  card builder instance
	 */
	public static Card.Builder toCardBuilder(Message message) {
		final Card.Builder builder = new Card.Builder();
		toAbstractEntity(builder, message);
		Collection<String> values = message.getValues(Constants.Messaging.Key.CARD_TYPE);
		if (! values.isEmpty()) {
			builder.type(Type.valueOf(values.iterator().next()));
		}
		values = message.getValues(Constants.Messaging.Key.CARD_FRONT_SIDE_TEXT);
		if (! values.isEmpty()) {
			builder.frontSideText(values.iterator().next());
		}
		values = message.getValues(Constants.Messaging.Key.CARD_BACK_SIDE_TEXT);
		if (! values.isEmpty()) {
			builder.backSideText(values.iterator().next());
		}
		return builder;
	}
}
