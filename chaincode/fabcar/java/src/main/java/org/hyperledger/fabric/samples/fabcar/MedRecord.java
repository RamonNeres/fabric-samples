/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.samples.fabcar;

import java.util.Objects;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

import com.owlike.genson.annotation.JsonProperty;

@DataType()
public final class MedRecord {

    public String getUserId() { return userId; }

    public String getFileName() { return fileName; }

    public String getAuthorityID() {
        return authorityID;
    }

    public String getGlobalParID() {
        return globalParID;
    }

    public String getHash() { return hash; }

    @Property()
    private final String userId;

    @Property()
    private final String fileName;

    @Property()
    private final String authorityID;

    @Property()
    private final String globalParID;

    @Property()
    private final String hash;

    public MedRecord(@JsonProperty("userId") final String userId, @JsonProperty("fileName") final String fileName,
                     @JsonProperty("hash") final String hash, @JsonProperty("authorityID") final String authorityID,
                     @JsonProperty("globalParID") final String globalParID) {
        this.userId = userId;
        this.fileName = fileName;
        this.hash = hash;
        this.authorityID = authorityID;
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
