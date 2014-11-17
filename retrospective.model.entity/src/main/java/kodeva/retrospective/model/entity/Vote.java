package kodeva.retrospective.model.entity;

/**
 * Entity representing a vote.
 */
public final class Vote extends AbstractEntity {
	private String cardId;
	private String userDeskId;
	private String sessionId;
	
	private Vote() {
		super();
	}

	public String getCardId() {
		return cardId;
	}

	public String getUserDeskId() {
		return userDeskId;
	}

	public String getSessionId() {
		return sessionId;
	}

	@Override
	public boolean equals(Object anObject) {
		if (super.equals(anObject)) {
	        if (anObject instanceof Vote) {
            	return true;
	        }
		}
        return false;
    }

	/**
	 * Builder of Vote instances.
	 */
	public static final class Builder {
		private Card card;
		private String userDeskId;
		private Session session;

		/**
		 * Sets the card to which the vote belongs.
		 * @param card
		 *  card instance
		 * @return
		 *  this instance
		 */
		public Builder card(Card card) {
			this.card = card;
			return this;
		}

		/**
		 * Sets id of the user to whom the vote belongs.
		 * @param userDeskId
		 *  UserDesk ID
		 * @return
		 *  this instance
		 */
		public Builder userDeskId(String userDeskId) {
			this.userDeskId = userDeskId;
			return this;
		}

		/**
		 * Sets the retrospective session to which the vote belongs.
		 * @param session
		 *  session instance
		 * @return
		 *  this instance
		 */
		public Builder session(Session session) {
			this.session = session;
			return this;
		}

		/**
		 * Builds new vote instance.
		 * @return
		 *  Card instance
		 */
		public Vote build() {
			if (card == null) {
				throw new IllegalArgumentException("Card not specified");
			}
			if (userDeskId == null) {
				throw new IllegalArgumentException("UserDesk ID not specified");
			}
			if (session == null) {
				throw new IllegalArgumentException("Session not specified");
			}
			Vote vote = new Vote();
			vote.cardId = card.getId();
			vote.userDeskId = userDeskId;
			vote.sessionId = session.getId();
			return vote;
		}
	}
}
