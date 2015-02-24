package kodeva.retrospective.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import kodeva.retrospective.messaging.Message;
import kodeva.retrospective.messaging.MessageBroker;
import kodeva.retrospective.model.entity.AbstractEntity;
import kodeva.retrospective.model.entity.Card;
import kodeva.retrospective.model.entity.PinWall;
import kodeva.retrospective.model.entity.Session;
import kodeva.retrospective.model.entity.User;
import kodeva.retrospective.model.entity.UserDesk;
import kodeva.retrospective.model.entity.Vote;

public class Model {
	private final MessageBroker messageBroker;
	private final PinWall pinWall;
	private final UserDesk userDesk;
	private final User user;
	private final Session session;
	private final Map<Card, AbstractEntity> cardsOnUserDesk;
	private final Map<Card, AbstractEntity> cardsOnPinWall;
	private final Set<Vote> votes;
	private int modelVersion;

	public Model(MessageBroker messageBroker) {
		this.messageBroker = messageBroker;
		user = new User.Builder().name("").build();
		session = new Session.Builder().name("").build();
		userDesk = new UserDesk.Builder().user(user).build();
		pinWall = new PinWall.Builder().session(session).build();
		cardsOnUserDesk = Collections.synchronizedMap(new HashMap<Card, AbstractEntity>());
		cardsOnPinWall = Collections.synchronizedMap(new HashMap<Card, AbstractEntity>());
		votes = Collections.synchronizedSet(new HashSet<Vote>());
		modelVersion = 0;
	}

	/**
	 * @return
	 *  current model version
	 */
	public int getModelVersion() {
		return modelVersion;
	}
	
	/**
	 * Creates a new card on user desk.
	 * @param card
	 *  card instance
	 */
	public final void createCard(Card card) {
		if (! cardsOnUserDesk.containsKey(card) && ! cardsOnPinWall.containsKey(card)) {
			cardsOnUserDesk.put(card, userDesk);
			messageBroker.sendMessage(createMessageBuilder()
					.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_ADD))
					.entries(EntityMessageAdapter.toMessageEntries(card)).build());
		}
	}

	/**
	 * Deletes the card from user desk.
	 * @param card
	 *  card instance
	 * @param userDeskId
	 *  id of user desk from which the card should be removed
	 */
	public final void deleteCard(Card card) {
		deleteCard(card, userDesk.getId());
	}

	/**
	 * Deletes the card from user desk.
	 * @param card
	 *  card instance
	 * @param userDeskId
	 *  id of user desk from which the card should be removed
	 */
	public final synchronized void deleteCard(Card card, String userDeskId) {
		if (userDesk.getId().equals(userDeskId)) {
			if (cardsOnUserDesk.remove(card) != null) {
				messageBroker.sendMessage(createMessageBuilder().entries(EntityMessageAdapter.toMessageEntries(card))
						.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_DELETE))
						.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.USER_DESK_ID, userDeskId))
						.build());
			}
		}

		final Iterator<Vote> votesIter = votes.iterator();
		while (votesIter.hasNext()) {
			if (card.getId().equals(votesIter.next().getCardId())) {
				votesIter.remove();
			}
			
		}
	}

	/**
	 * Makes a card from given UserDesk visible in session.
	 * @param card
	 *  card instance
	 * @param userDeskId
	 *  UserDesk ID
	 */
	public final synchronized void publishCard(Card card, String userDeskId) {
		cardsOnUserDesk.remove(card);
		cardsOnPinWall.put(card, pinWall);
		messageBroker.sendMessage(createMessageBuilder()
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_PUBLISH))
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.USER_DESK_ID, userDeskId))
				.entries(EntityMessageAdapter.toMessageEntries(card)).build());
	}

	/**
	 * Unpublishes a card from PinWall.
	 * @param card
	 *  card instance
	 * @param userDeskId
	 *  UserDesk ID
	 */
	public final synchronized void unpublishCard(Card card, String userDeskId) {
		cardsOnPinWall.remove(card);
		if (userDesk.getId().equals(userDeskId)) {
			cardsOnUserDesk.put(card, userDesk);
		}
		messageBroker.sendMessage(createMessageBuilder()
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_UNPUBLISH))
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.USER_DESK_ID, userDeskId))
				.entries(EntityMessageAdapter.toMessageEntries(card)).build());
	}

	/**
	 * Updates the card on UserDesk.
	 * @param cardOrig
	 *  original card
	 * @param cardNew
	 *  new card
	 */
	public final synchronized void updateCard(Card cardOrig, Card cardNew) {
		if (! cardsOnUserDesk.containsKey(cardOrig)) {
			return;
		}

		final AbstractEntity cardsContainerEntity = cardsOnUserDesk.remove(cardOrig);
		cardsOnUserDesk.put(cardNew, cardsContainerEntity);
		messageBroker.sendMessage(createMessageBuilder()
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_UPDATE))
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.CARD_CONTAINER_TYPE, Constants.Messaging.Value.KEY_CARD_CONTAINER_TYPE_USER_DESK))
				.entries(EntityMessageAdapter.toMessageEntries(cardNew)).build());
	}
	
	/**
	 * Adds one vote to the card on PinWall for specified UserDesk (user).
	 * @param card
	 *  card instance
	 * @param userDeskId
	 *  UserDesk ID
	 */
	public final synchronized void addVote(Card card, String userDeskId) {
		if (! cardsOnPinWall.containsKey(card)) {
			return;
		}

		final Session session = pinWall.getSession();
		if ((userDeskId == null) || (session == null)) {
			return;
		}

		if (getUserDeskVotes(userDeskId).size() < Constants.MAXIMUM_VOTES_PER_USER_PER_SESSION) {
			final Vote vote = new Vote.Builder().card(card).userDeskId(userDeskId).session(session).build();
			votes.add(vote);
			messageBroker.sendMessage(createMessageBuilder()
					.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_VOTE_ADD))
					.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.USER_DESK_ID, userDeskId))
					.entries(EntityMessageAdapter.toMessageEntries(card)).build());
		}
	}
	
	/**
	 * Removes one vote from the card on pin wall for specified UserDesk (user).
	 * @param card
	 *  card instance
	 * @param userDeskId
	 *  UserDesk ID
	 */
	public final synchronized void removeVote(Card card, String userDeskId) {
		if (! cardsOnPinWall.containsKey(card)) {
			return;
		}

		final Session session = pinWall.getSession();
		if ((userDeskId == null) || (session == null)) {
			return;
		}
		
		synchronized(votes) {
			Vote voteToRemove = null;
			for (Vote vote : votes) {
				if (card.getId().equals(vote.getCardId()) && userDeskId.equals(vote.getUserDeskId()) && session.getId().equals(vote.getSessionId())) {
					voteToRemove = vote;
				}
			}
			
			if (voteToRemove != null) {
				votes.remove(voteToRemove);
				messageBroker.sendMessage(createMessageBuilder()
						.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_VOTE_REMOVE))
						.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.USER_DESK_ID, userDeskId))
						.entries(EntityMessageAdapter.toMessageEntries(card)).build());
			}
		}
	}
	
	/**
	 * Sums up own votes assigned to given card.
	 * @param card
	 *  card instance
	 * @return
	 *  count of own votes assigned to the card
	 */
	public final synchronized int getVotesCountOwn(Card card) {
		int votesCount = 0;
		for (Vote vote : votes) {
			if (card.getId().equals(vote.getCardId()) && userDesk.getId().equals(vote.getUserDeskId())) {
				votesCount++;
			}
		}
		return votesCount;
	}
	
	/**
	 * Sums up all votes assigned to given card.
	 * @param card
	 *  card instance
	 * @return
	 *  count of all votes assigned to the card
	 */
	public final synchronized int getVotesCountTotal(Card card) {
		int votesCount = 0;
		for (Vote vote : votes) {
			if (card.getId().equals(vote.getCardId())) {
				votesCount++;
			}
		}
		return votesCount;
	}

	public final synchronized String serializeForSynchronization() {
		//TODO 3: Method that increments model version and serializes pinwall, votes and model version into string - does not allow for concurrent operations
		try {
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			final ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(cardsOnPinWall.keySet());
			oos.close();
			return baos.toString(StandardCharsets.UTF_8.name());
		} catch (IOException e) {
			throw new RuntimeException("Cannot serialize model", e);
		}
	}
	
	public final synchronized void synchronize(final String modelStr) {
		//TODO 6: Method that initializes model based on received string (serialized pinwall, votes)
		try {
			final ByteArrayInputStream bais = new ByteArrayInputStream(modelStr.getBytes(StandardCharsets.UTF_8.name()));
			final ObjectInputStream ois = new ObjectInputStream(bais);
			Set<Card> cards = (Set<Card>) ois.readObject();
			ois.close();
		} catch (Exception e) {
			throw new RuntimeException("Cannot deserialize model", e);
		}
	}
	
	/**
	 * @return
	 *  Pin wall entity.
	 */
	public PinWall getPinWall() {
		return pinWall;
	}

	/**
	 * @return
	 *  User desk entity.
	 */
	public UserDesk getUserDesk() {
		return userDesk;
	}
	
	/**
	 * @param userDeskId
	 *  UserDesk ID
	 * @return user's votes for given UserDesk
	 */
	private final Set<Vote> getUserDeskVotes(String userDeskId) {
		final Session session = pinWall.getSession();
		if ((userDeskId == null) || (session == null)) {
			return Collections.emptySet();
		}
		
		final Set<Vote> userSessionVotes = new HashSet<>();
		synchronized(votes) {
			for (Vote vote : votes) {
				if (userDeskId.equals(vote.getUserDeskId()) && session.getId().equals(vote.getSessionId())) {
					userSessionVotes.add(vote);
				}
			}
		}

		return userSessionVotes;
	}
	
	private Message.Builder createMessageBuilder() {
		return new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.MODEL_VERSION, Integer.toString(modelVersion)));
	}
}
