package com.taf.auto.data;

/**
 * The abstract superclass for keys used with {@link DataMap}. The subclass must provide the type T to allow
 * strongly typed access. Simply create a concrete subclass and its full classname (includes the package) will uniquely
 * identify the key.
 *
 * @see DataMap
 */
public abstract class DataKey<T> {
    /**
     * A key equals another key with the same classname.
     * @param   obj   the reference object with which to compare.
     * @return  {@code true} if this object is the same as the obj
     *          argument; {@code false} otherwise.
     */
    public final boolean equals(Object obj) {
        /** Only consider objects that are also DataKeys. This also excludes a null value */
        if(!(obj instanceof DataKey)) {
            return false;
        }

        return getClass().getName().equals(obj.getClass().getName());
    }

    /**
     * Uses the full classname to hash with.
     * @return the hashcode
     */
    public final int hashCode() {
        return getClass().getName().hashCode();
    }
}
