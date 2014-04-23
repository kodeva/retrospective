package kodeva.retrospective.view;

/**
 * Constants used by View component.
 */
public final class Constants {
	private Constants() {
	}

	/**
	 * Messaging constants.
	 */
	public static final class Messaging {
		private Messaging() {
		}
		
		/**
		 * Sender identification of the View component.
		 */
		public static final String SENDER = "retrospective.client:kodeva.retrospective.view";

		/**
		 * Message entry keys.
		 */
		public static final class Key {
			private Key() {
			}
			
			/**
			 * Message key for UI events.
			 */
			public static final String EVENT = SENDER + ":event";
			
			/**
			 * Message key for updated value.
			 */
			public static final String UPDATED_VALUE = SENDER + ":updated-value";
		}

		/**
		 * Message entry values.
		 */
		public static final class Value {
			private Value() {
			}
			
			/**
			 * Event message key for creating Went Well card.
			 */
			public static final String KEY_EVENT_CARD_CREATE_WENT_WELL = "card:create:WentWell";

			/**
			 * Event message key for creating Needs Improvement card.
			 */
			public static final String KEY_EVENT_CARD_CREATE_NEEDS_IMPROVEMENT = "card:create:NeedsImprovement";

			/**
			 * Event message key for removing card.
			 */
			public static final String KEY_EVENT_CARD_DELETE = "card:delete";

			/**
			 * Event message key for update of card's front side text.
			 */
			public static final String KEY_EVENT_CARD_UPDATE_FRONT_SIDE_TEXT = "card:update:front-side-text";

			/**
			 * Event message key for posting the card to pin wall.
			 */
			public static final String KEY_EVENT_CARD_POSTIT = "card:postit";

			/**
			 * Event message key for editing the card on pin wall.
			 */
			public static final String KEY_EVENT_CARD_EDIT = "card:edit";

			/**
			 * Event message key for incrementing votes on the card on pin wall.
			 */
			public static final String KEY_EVENT_CARD_VOTES_INCREMENT = "card:votes:increment";

			/**
			 * Event message key for decrementing votes on the card on pin wall.
			 */
			public static final String KEY_EVENT_CARD_VOTES_DECREMENT = "card:votes:decrement";

			/**
			 * Event message key for connection to retrospective session.
			 */
			public static final String KEY_EVENT_SESSION_CONNECT = "session:connect";

			/**
			 * Event message key for disconnection from retrospective session.
			 */
			public static final String KEY_EVENT_SESSION_DISCONNECT = "session:disconnect";

			/**
			 * Event message key for starting a new retrospective session.
			 */
			public static final String KEY_EVENT_SESSION_START = "session:start";

			/**
			 * Event message key for terminating the retrospective session.
			 */
			public static final String KEY_EVENT_SESSION_TERMINATE = "session:terminate";
		}
	}
}
