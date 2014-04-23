package kodeva.retrospective.model.entity;


public final class Card extends AbstractEntity {
	private Type type;
	private String frontSideText, backSideText;

	private Card() {
		super();
	}
	
	public Type getType() {
		return type;
	}

	public String getFrontSideText() {
		return frontSideText;
	}
	
	public String getBackSideText() {
		return backSideText;
	}

	@Override
	public boolean equals(Object anObject) {
		if (super.equals(anObject)) {
	        if (anObject instanceof Card) {
            	return true;
	        }
		}
        return false;
    }

	/**
	 * Card type
	 */
	public enum Type {
		WentWell,
		NeedsImprovement;
	}

	/**
	 * Builder of Card instances.
	 */
	public static final class Builder extends AbstractEntity.Builder {
		private Type type;
		private String frontSideText, backSideText;

		/**
		 * Set card data from card instance.
		 * @param card
		 *  card instance
		 * @return
		 *  this instance
		 */
		public Builder card(Card card) {
			abstractEntity(card);
			type = card.getType();
			frontSideText = card.getFrontSideText();
			backSideText = card.getBackSideText();
			return this;
		}

		/**
		 * Sets card type.
		 * @param type
		 *  card type
		 * @return
		 *  this instance
		 */
		public Builder type(Type type) {
			this.type = type;
			return this;
		}

		/**
		 * Sets card front side text.
		 * @param frontSideText
		 *  card front side text
		 * @return
		 *  this instance
		 */
		public Builder frontSideText(String frontSideText) {
			this.frontSideText = frontSideText;
			return this;
		}

		/**
		 * Sets card back side text.
		 * @param backSideText
		 *  card back side text
		 * @return
		 *  this instance
		 */
		public Builder backSideText(String backSideText) {
			this.backSideText = backSideText;
			return this;
		}

		/**
		 * Builds new card instance.
		 * @return
		 *  Card instance
		 */
		public Card build() {
			if (type == null) {
				throw new IllegalArgumentException("Card type not specified");
			}
			Card card = new Card();
			super.build(card);
			card.type = type;
			if (frontSideText != null) {
				card.frontSideText = frontSideText;
			}
			if (backSideText != null) {
				card.backSideText = backSideText;
			}
			return card;
		}
	}
}
