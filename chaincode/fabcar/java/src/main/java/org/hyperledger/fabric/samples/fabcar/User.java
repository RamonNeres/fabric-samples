package org.hyperledger.fabric.samples.fabcar;

import java.util.Objects;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

import com.owlike.genson.annotation.JsonProperty;

@DataType()
public final class User {

    public String getId() {
        return id;
    }

    public String[] getAttributes() {
        return attributes;
    }

    @Property()
    private final String id;

    @Property()
    private final String[] attributes;

    public User(@JsonProperty("id") final String id, @JsonProperty("attributes") final String... attributes) {
        this.id = id;
        this.attributes = attributes.clone();
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }

        User other = (User) obj;

        return Objects.deepEquals(getId(), other.getId()) &&
                Objects.deepEquals(getAttributes(), other.getAttributes());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getAttributes());
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "@" + Integer.toHexString(hashCode()) + " [id=" + id + ", attributes="
                + attributes + "]";
    }
}

