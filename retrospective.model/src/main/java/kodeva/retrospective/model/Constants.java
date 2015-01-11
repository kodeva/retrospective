package kodeva.retrospective.model;

/**
 * Constants used by Model component.
 */
public final class Constants {
	private Constants() {
	}

	/**
	 * Maximum count of user votes in one session.
	 */
	public static final int MAXIMUM_VOTES_PER_USER_PER_SESSION = 3;

	/**
	 * Messaging constants.
	 */
	public static final class Messaging {
		private Messaging() {
		}
		
		/**
		 * Sender identification of the View component.
		 */
		public static final String SENDER = "retrospective.model:kodeva.retrospective.model";

		/**
		 * Message entry keys.
		 */
		public static final class Key {
			private Key() {
			}
			
			/**
			 * Message key for data model events.
			 */
			public static final String EVENT = SENDER + ":event";
			
			/**
			 * Message key for card container type.
			 */
			public static final String CARD_CONTAINER_TYPE = SENDER + ":card:container:type";
			
			/**
			 * Message key for card type.
			 */
			static final String ENTITY_ID = SENDER + ":entity:id";
			
			/**
			 * Message key for card type.
			 */
			static final String CARD_TYPE = SENDER + ":card:type";
			
			/**
			 * Message key for card front side text.
			 */
			static final String CARD_FRONT_SIDE_TEXT = SENDER + ":card:front-side-text";
			
			/**
			 * Message key for card back side text.
			 */
			static final String CARD_BACK_SIDE_TEXT = SENDER + ":card:back-side-text";
			
			/**
			 * Message key for user desk id.
			 */
			public static final String USER_DESK_ID = SENDER + ":user-desk-id";
			
			/**
			 * Message key for pin wall id.
			 */
			public static final String PIN_WALL_ID = SENDER + ":pin-wall-id";
			
			/**
			 * Message key for model version:
			 * - messages with older versions are ignored
			 * - messages with newer versions are queued until model sync message comes
			 */
			public static final String MODEL_VERSION = SENDER + ":model-version";
		}

		/**
		 * Message entry values.
		 */
		public static final class Value {
			private Value() {
			}
			
			/**
			 * Event message key for adding card to card container.
			 */
			public static final String KEY_EVENT_CARD_ADD = "card:add";
			
			/**
			 * Event message key for removing card from card container.
			 */
			public static final String KEY_EVENT_CARD_DELETE = "card:delete";
			
			/**
			 * Event message key for updating card in card container.
			 */
			public static final String KEY_EVENT_CARD_UPDATE = "card:update";
			
			/**
			 * Event message key for moving card from local to session card container.
			 */
			public static final String KEY_EVENT_CARD_PUBLISH = "card:publish";
			
			/**
			 * Event message key for moving card from session to local card container.
			 */
			public static final String KEY_EVENT_CARD_UNPUBLISH = "card:unpublish";

			/**
			 * Event message key for adding a vote to a card.
			 */
			public static final String KEY_EVENT_VOTE_ADD = "vote:add";

			/**
			 * Event message key for removing a vote from a card.
			 */
			public static final String KEY_EVENT_VOTE_REMOVE = "vote:remove";

			/**
			 * User desk card container type.
			 */
			public static final String KEY_CARD_CONTAINER_TYPE_USER_DESK = "user-desk";
			
			/**
			 * Pin wall card container type.
			 */
			public static final String KEY_CARD_CONTAINER_TYPE_PINWALL = "pinwall";
		}
	}
}
