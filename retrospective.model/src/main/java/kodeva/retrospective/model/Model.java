package kodeva.retrospective.model;

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

	public Model(MessageBroker messageBroker) {
		this.messageBroker = messageBroker;
		user = new User.Builder().name("").build();
		session = new Session.Builder().name("").build();
		userDesk = new UserDesk.Builder().user(user).build();
		pinWall = new PinWall.Builder().session(session).build();
		cardsOnUserDesk = Collections.synchronizedMap(new HashMap<Card, AbstractEntity>());
		cardsOnPinWall = Collections.synchronizedMap(new HashMap<Card, AbstractEntity>());
		votes = Collections.synchronizedSet(new HashSet<Vote>());
	}

	/**
	 * Creates a new card on user desk.
	 * @param card
	 *  card instance
	 */
	public final void createCard(Card card) {
		if (! cardsOnUserDesk.containsKey(card) && ! cardsOnPinWall.containsKey(card)) {
			cardsOnUserDesk.put(card, userDesk);
			messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
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
	public final void deleteCard(Card card, String userDeskId) {
		if (userDesk.getId().equals(userDeskId)) {
			cardsOnUserDesk.remove(card);
		}
		messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER).entries(EntityMessageAdapter.toMessageEntries(card))
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_DELETE))
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.USER_DESK_ID, userDeskId))
				.build());
		synchronized(votes) {
			final Iterator<Vote> votesIter = votes.iterator();
			while (votesIter.hasNext()) {
				if (card.getId().equals(votesIter.next().getCardId())) {
					votesIter.remove();
				}
				
			}
		}
	}

	/**
	 * Makes a card visible in session.
	 * @param card
	 *  card instance
	 */
	public final void publishCard(Card card) {
		cardsOnUserDesk.remove(card);
		cardsOnPinWall.put(card, pinWall);
		messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_PUBLISH))
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.USER_DESK_ID, userDesk.getId()))
				.entries(EntityMessageAdapter.toMessageEntries(card)).build());
	}

	/**
	 * Unpublish a card from PinWall.
	 * @param card
	 *  card instance
	 */
	public final void unpublishCard(Card card) {
		cardsOnPinWall.remove(card);
		cardsOnUserDesk.put(card, userDesk);
		messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_UNPUBLISH))
				.entries(EntityMessageAdapter.toMessageEntries(card)).build());
	}

	/**
	 * Updates the card on User desk or PinWall.
	 * @param cardOrig
	 *  original card
	 * @param cardNew
	 *  new card
	 */
	public final void updateCard(Card cardOrig, Card cardNew) {
		final Map<Card, AbstractEntity> cardsContainer;
		final String cardContainerType;
		if (cardsOnUserDesk.containsKey(cardOrig)) {
			cardsContainer = cardsOnUserDesk;
			cardContainerType = Constants.Messaging.Value.KEY_CARD_CONTAINER_TYPE_USER_DESK;
		} else if (cardsOnUserDesk.containsKey(cardOrig)) {
			cardsContainer = cardsOnPinWall;
			cardContainerType = Constants.Messaging.Value.KEY_CARD_CONTAINER_TYPE_PINWALL;
		} else {
			return;
		}

		final AbstractEntity cardsContainerEntity = cardsContainer.remove(cardOrig);
		cardsContainer.put(cardNew, cardsContainerEntity);
		messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_UPDATE))
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.CARD_CONTAINER_TYPE, cardContainerType))
				.entries(EntityMessageAdapter.toMessageEntries(cardNew)).build());
	}
	
	/**
	 * Adds one vote to the card on PinWall for current user.
	 * @param card
	 *  card instance
	 */
	public final void addVote(Card card) {
		if (! cardsOnPinWall.containsKey(card)) {
			return;
		}

		final User user = userDesk.getUser();
		final Session session = pinWall.getSession();
		if ((user == null) || (session == null)) {
			return;
		}

		if (getUserVotes().size() < Constants.MAXIMUM_VOTES_PER_USER_PER_SESSION) {
			final Vote vote = new Vote.Builder().card(card).user(user).session(session).build();
			votes.add(vote);
			messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
					.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_VOTE_ADD))
					.entries(EntityMessageAdapter.toMessageEntries(card)).build());
		}
	}
	
	/**
	 * Removes one vote from the card on pin wall for current user.
	 * @param card
	 *  card instance
	 */
	public final void removeVote(Card card) {
		if (! cardsOnPinWall.containsKey(card)) {
			return;
		}

		final User user = userDesk.getUser();
		final Session session = pinWall.getSession();
		if ((user == null) || (session == null)) {
			return;
		}
		
		synchronized(votes) {
			for (Vote vote : votes) {
				if (card.getId().equals(vote.getCardId()) && user.getId().equals(vote.getUserId()) && session.getId().equals(vote.getSessionId())) {
					votes.remove(vote);
					messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
							.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_VOTE_REMOVE))
							.entries(EntityMessageAdapter.toMessageEntries(card)).build());
				}
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
	public final int getVotesCountOwn(Card card) {
		int votesCount = 0;
		synchronized(votes) {
			for (Vote vote : votes) {
				if (card.getId().equals(vote.getCardId()) && user.getId().equals(vote.getUserId())) {
					votesCount++;
				}
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
	public final int getVotesCountTotal(Card card) {
		int votesCount = 0;
		synchronized(votes) {
			for (Vote vote : votes) {
				if (card.getId().equals(vote.getCardId())) {
					votesCount++;
				}
			}
		}
		return votesCount;
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
	 * @return user's votes
	 */
	private final Set<Vote> getUserVotes() {
		final User user = userDesk.getUser();
		final Session session = pinWall.getSession();
		if ((user == null) || (session == null)) {
			return Collections.emptySet();
		}
		
		final Set<Vote> userSessionVotes = new HashSet<>();
		synchronized(votes) {
			for (Vote vote : votes) {
				if (user.getId().equals(vote.getUserId()) && session.getId().equals(vote.getSessionId())) {
					userSessionVotes.add(vote);
				}
			}
		}

		return userSessionVotes;
	}
}
