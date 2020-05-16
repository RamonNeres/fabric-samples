package org.example;

import java.util.Hashtable;

public class Authority {

    public static class Policy {
        private final String eg1g1a1;

        private final String g1yi;

        public String getEg1g1a1() {
            return eg1g1a1;
        }

        public String getG1yi() {
            return g1yi;
        }

        public Policy(final String eg1g1a1, final String g1yi) {
            this.eg1g1a1 = eg1g1a1;
            this.g1yi = g1yi;
        }
    }

    private final String authorityID;

    private final Hashtable<String, Policy> publicKeys;

    public String getAuthorityID() {
        return authorityID;
    }

    public void addPublicKey(final String policy, final String eg1g1a1, final String g1yi) {
        publicKeys.put(policy, new Policy(eg1g1a1, g1yi));
    }

    public Hashtable<String, Policy> getPublicKeys() {
        return publicKeys;
    }

    public Authority(final String authorityID, final Hashtable<String, Policy> publicKeys) {
        this.authorityID = authorityID;
        this.publicKeys = (Hashtable<String, Policy>)(publicKeys.clone());
    }

    public Authority(final String authorityID, final String policy, String eg1g1a1, final String g1yi) {
        this.authorityID = authorityID;
        publicKeys = new Hashtable<String, Policy>();
        publicKeys.put(policy, new Policy(eg1g1a1, g1yi));
    }

    public Authority(final String authorityID) {
        this.authorityID = authorityID;
        publicKeys = new Hashtable<String, Policy>();
    }

}
