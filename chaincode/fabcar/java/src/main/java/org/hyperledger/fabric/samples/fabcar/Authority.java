package org.hyperledger.fabric.samples.fabcar;

import com.owlike.genson.annotation.JsonCreator;
import com.owlike.genson.annotation.JsonProperty;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

import java.util.Hashtable;

@DataType()
public class Authority {

    @DataType()
    public static class Policy {
        @Property()
        private final String eg1g1a1;

        @Property()
        private final String g1yi;

        public String getEg1g1a1() {
            return eg1g1a1;
        }

        public String getG1yi() {
            return g1yi;
        }

        public Policy(@JsonProperty("eg1g1a1") final String eg1g1a1, @JsonProperty("g1yi") final String g1yi) {
            this.eg1g1a1 = eg1g1a1;
            this.g1yi = g1yi;
        }
    }

    @Property()
    private final String authorityID;

    @Property()
    private final Hashtable<String, Policy> publicKeys;

    public String getAuthorityID() {
        return authorityID;
    }

    public void addPublicKey(@JsonProperty("policy") final String policy, @JsonProperty("eg1g1a1") final String eg1g1a1,
                             @JsonProperty("g1yi") final String g1yi) {
        publicKeys.put(policy, new Policy(eg1g1a1, g1yi));
    }

    public Hashtable<String, Policy> getPublicKeys() {
        return publicKeys;
    }

    @JsonCreator
    public Authority(@JsonProperty("authorityID") final String authorityID,
                     @JsonProperty("publicKeys") final Hashtable<String, Policy> publicKeys) {
        this.authorityID = authorityID;
        this.publicKeys = (Hashtable<String, Policy>)(publicKeys.clone());
    }

    public Authority(@JsonProperty("authorityID") final String authorityID, @JsonProperty("policy") final String policy,
                     @JsonProperty("eg1g1a1") final String eg1g1a1, @JsonProperty("g1yi") final String g1yi) {
        this.authorityID = authorityID;
        publicKeys = new Hashtable<String, Policy>();
        publicKeys.put(policy, new Policy(eg1g1a1, g1yi));
    }

    public Authority(@JsonProperty("authorityID") final String authorityID) {
        this.authorityID = authorityID;
        publicKeys = new Hashtable<String, Policy>();
    }

}
