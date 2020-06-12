package org.example;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public final class User implements Serializable {

    public String getId() {
        return id;
    }

    public ArrayList<String> getAttributes() {
        return attributes;
    }

    private final String id;

    private final ArrayList<String> attributes;

    @JsonCreator
    public User(@JsonProperty("id") final String id, @JsonProperty("attributes") final ArrayList<String> attributes) {
        this.id = id;
        this.attributes = (ArrayList<String>)attributes.clone();
    }

    public User(@JsonProperty("id") final String id, @JsonProperty("attributes") final String... attributes) {
        this.id = id;
        this.attributes = new ArrayList<String>();
        this.attributes.addAll(Arrays.asList(attributes));
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