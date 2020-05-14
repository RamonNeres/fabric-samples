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

    public String getUserId() {
        return userId;
    }

    public String getBucket() {
        return bucket;
    }

    public String getAuthorityID() {
        return authorityID;
    }

    public String getGlobalParID() {
        return globalParID;
    }

    @Property()
    private final String userId;

    @Property()
    private final String bucket;

    @Property()
    private final String authorityID;

    @Property()
    private final String globalParID;

    public MedRecord(@JsonProperty("userId") final String userId, @JsonProperty("bucket") final String bucket,
                     @JsonProperty("authorityID") final String authorityID, @JsonProperty("globalParID") final String globalParID) {
        this.userId = userId;
        this.bucket = bucket;
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

        return Objects.deepEquals(new String[] {getUserId(), getBucket()},
                new String[] {other.getUserId(), other.getBucket()});
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUserId(), getBucket());
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "@" + Integer.toHexString(hashCode()) + " [userId=" + userId + ", bucket="
                + bucket + "]";
    }
}
