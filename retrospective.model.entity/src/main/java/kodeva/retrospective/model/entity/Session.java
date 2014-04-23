package kodeva.retrospective.model.entity;

/**
 * Entity representing a retrospective session.
 */
public final class Session extends AbstractEntity {
	private String name;
	
	private Session() {
		super();
	}

	public String getName() {
		return name;
	}
	
	@Override
	public boolean equals(Object anObject) {
		if (super.equals(anObject)) {
	        if (anObject instanceof Session) {
            	return true;
	        }
		}
        return false;
    }

	/**
	 * Builder of Session instances.
	 */
	public static final class Builder {
		private String name;

		/**
		 * Sets name of the session.
		 * @param name
		 *  retrospective session name
		 * @return
		 *  this instance
		 */
		public Builder name(String name) {
			this.name = name;
			return this;
		}

		/**
		 * Builds new Session instance.
		 * @return
		 *  Session instance
		 */
		public Session build() {
			if (name == null) {
				throw new IllegalArgumentException("Name not specified");
			}
			Session session = new Session();
			session.name = name;
			return session;
		}
	}
}
