package kodeva.retrospective.model.entity;

import java.util.UUID;


/**
 * Abstract entity.
 */
public abstract class AbstractEntity {
	private String id;
	
	protected AbstractEntity() {
		id = UUID.randomUUID().toString();
	}
	
	public String getId() {
		return id;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}
	

	@Override
	public boolean equals(Object anObject) {
        if (this == anObject) {
            return true;
        }
        if (anObject instanceof AbstractEntity) {
        	AbstractEntity anotherAbstractEntity = (AbstractEntity) anObject;
            if (id.equals(anotherAbstractEntity.id)) {
            	return true;
            }
        }
        return false;
    }

	/**
	 * Builder for entity instances
	 */
	public static abstract class Builder {
		private String id;

		/**
		 * Sets entity data from entity instance.
		 * @param entity
		 *  entity instance
		 */
		protected void abstractEntity(AbstractEntity entity) {
			id = entity.id;
		}
		
		/**
		 * Sets entity id.
		 * @param id
		 *  entity id
		 */
		public void id(String id) {
			this.id = id;
		}

		/**
		 * Adds abstract entity data into the entity instance
		 * @param entity
		 *  entity instance
		 */
		protected void build(AbstractEntity entity) {
			if (id != null) {
				entity.id = id;
			}
		}
	}
}
