package org.hyperledger.fabric.samples.fabcar;

import com.owlike.genson.annotation.JsonCreator;
import com.owlike.genson.annotation.JsonProperty;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@DataType()
public class Authority implements Serializable {

    @DataType()
    public static class Policy implements Serializable {
        @Property()
        private final String eg1g1ai;

        @Property()
        private final String g1yi;

        public String getEg1g1ai() {
            return eg1g1ai;
        }

        public String getG1yi() {
            return g1yi;
        }

        @JsonCreator
        public Policy(@JsonProperty("eg1g1ai") final String eg1g1ai, @JsonProperty("g1yi") final String g1yi) {
            this.eg1g1ai = eg1g1ai;
            this.g1yi = g1yi;
        }
    }

    @Property()
    private final String authorityID;

    @Property()
    private final HashMap<String, Policy> publicKeys;

    public String getAuthorityID() {
        return authorityID;
    }

    public void addPublicKey(final String policy, final String eg1g1ai, final String g1yi) {
        publicKeys.put(policy, new Policy(eg1g1ai, g1yi));
    }

    public Map<String, Policy> getPublicKeys() {
        return publicKeys;
    }

    @JsonCreator
    public Authority(@JsonProperty("authorityID") final String authorityID,
                     @JsonProperty("publicKeys") final HashMap<String, Policy> publicKeys) {
        this.authorityID = authorityID;
        this.publicKeys = (HashMap<String, Policy>)(publicKeys.clone());
    }

    public Authority(final String authorityID, final String policy, final String eg1g1ai, final String g1yi) {
        this.authorityID = authorityID;
        publicKeys = new HashMap<String, Policy>();
        publicKeys.put(policy, new Policy(eg1g1ai, g1yi));
    }

    public Authority(final String authorityID) {
        this.authorityID = authorityID;
        publicKeys = new HashMap<String, Policy>();
    }

}
