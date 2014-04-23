package kodeva.retrospective.model.entity;



public final class UserDesk extends AbstractEntity {
	private User user;

	private UserDesk() {
		super();
	}

	public User getUser() {
		return user;
	}

	/**
	 * Builder of UserDesk instances.
	 */
	public final static class Builder extends AbstractEntity.Builder {
		private User user;
		
		/**
		 * Sets user to whom the user desk belongs.
		 * @param user
		 *  user instance
		 * @return
		 *  this instance
		 */
		public Builder user(User user) {
			this.user = user;
			return this;
		}
		
		/**
		 * Builds new UserDesk instance
		 * @return
		 *  UserDesk instance
		 */
		public UserDesk build() {
			if (user == null) {
				throw new IllegalArgumentException("User not specified");
			}
			final UserDesk userDesk = new UserDesk();
			super.build(userDesk);
			userDesk.user = user;
			return userDesk;
		}
	}
}
