package kodeva.retrospective.controller;

/**
 * Constants used by Controller component.
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
		public static final String SENDER = "retrospective.client:kodeva.retrospective.controller";

		/**
		 * Message entry keys.
		 */
		public static final class Key {
			private Key() {
			}
			
			/**
			 * Message key for controller events.
			 */
			public static final String EVENT = SENDER + ":event";
			
			/**
			 * Message key for message during which processing an error occurred.
			 */
			public static final String ERROR_ORIGINAL_MESSAGE = SENDER + ":error:original-message";
			
			/**
			 * Message key for error stacktrace.
			 */
			public static final String ERROR_STACKTRACE = SENDER + ":error:stacktrace";
			
			/**
			 * Message key for a client id.
			 */
			public static final String CLIENT_ID = SENDER + ":client:id";
			
			/**
			 * Message key for a serialized model.
			 */
			public static final String SERIALIZED_MODEL = SENDER + ":serialized-model";
		}

		/**
		 * Message entry values.
		 */
		public static final class Value {
			private Value() {
			}
			
			/**
			 * Event message key for error.
			 */
			public static final String KEY_EVENT_ERROR = "error";
			
			/**
			 * Event message key for model synchronization request.
			 */
			public static final String KEY_EVENT_MODEL_SYNC_REQUEST = "model-sync-request";
			
			/**
			 * Event message key for model synchronization.
			 */
			public static final String KEY_EVENT_MODEL_SYNC = "model-sync";
		}
	}
}