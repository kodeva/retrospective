package kodeva.retrospective.model.entity;


/**
 * Entity representing a user.
 */
public final class User extends AbstractEntity {
	private String name;
	
	private User() {
		super();
	}

	public String getName() {
		return name;
	}

	@Override
	public boolean equals(Object anObject) {
		if (super.equals(anObject)) {
	        if (anObject instanceof User) {
            	return true;
	        }
		}
        return false;
    }

	/**
	 * Builder of User instances.
	 */
	public static final class Builder {
		private String name;

		/**
		 * Sets name of the user.
		 * @param name
		 *  user name
		 * @return
		 *  this instance
		 */
		public Builder name(String name) {
			this.name = name;
			return this;
		}

		/**
		 * Builds new User instance.
		 * @return
		 *  User instance
		 */
		public User build() {
			if (name == null) {
				throw new IllegalArgumentException("Name not specified");
			}
			User user = new User();
			user.name = name;
			return user;
		}
	}
}
