package kodeva.retrospective.model.entity;



public class PinWall extends AbstractEntity {
	private Session session;
	
	private PinWall() {
		super();
	}

	public Session getSession() {
		return session;
	}

	/**
	 * Builder of PinWall instances.
	 */
	public final static class Builder extends AbstractEntity.Builder {
		private Session session;
		
		/**
		 * Sets session to which the pin wall belongs.
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
		 * Builds new UserDesk instance
		 * @return
		 *  UserDesk instance
		 */
		public PinWall build() {
			if (session == null) {
				throw new IllegalArgumentException("Session not specified");
			}
			final PinWall pinWall = new PinWall();
			super.build(pinWall);
			pinWall.session = session;
			return pinWall;
		}
	}
}
