package org.example;/*
 * SPDX-License-Identifier: Apache-2.0
 */

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Objects;

//TODO: Corrigir equals, getHasCode e toString (no chaincode também).
public final class MedRecord implements Serializable {

    public String getUserId() { return userId; }

    public String getFileName() { return fileName; }

    public String getAuthorityID() {
        return authorityID;
    }

    public String getGlobalParID() {
        return globalParID;
    }

    public String getHash() { return hash; }

    private final String userId;

    private final String fileName;

    private final String authorityID;

    private final String globalParID;

    private final String hash;

    public MedRecord(@JsonProperty("userId") final String userId, @JsonProperty("fileName") final String fileName,
                     @JsonProperty("hash") final String hash, @JsonProperty("authorityIDs") final String authorityIDs,
                     @JsonProperty("globalParID") final String globalParID) {
        this.userId = userId;
        this.fileName = fileName;
        this.hash = hash;
        this.authorityID = authorityIDs;
        this.globalParID = globalParID;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }

        MedRecord other = (MedRecord) obj;

        return Objects.deepEquals(new String[] {getUserId(), getFileName()},
                new String[] {other.getUserId(), other.getFileName()});
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUserId(), getFileName());
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "@" + Integer.toHexString(hashCode()) + " [userId=" + userId + ", fileName="
                + fileName + "]";
    }
}
