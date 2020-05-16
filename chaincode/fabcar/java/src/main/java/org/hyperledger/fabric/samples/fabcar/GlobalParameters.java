package org.hyperledger.fabric.samples.fabcar;

import com.owlike.genson.annotation.JsonProperty;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

import java.util.Objects;

@DataType()
public class GlobalParameters {

    @Property()
    private final String pairingParameters;

    @Property()
    private final String g1;

    public String getPairingParameters() {
        return pairingParameters;
    }

    public String getG1() {
        return g1;
    }

    public GlobalParameters(@JsonProperty("pairingParameters") final String pairingParameters, @JsonProperty("g1") final String g1) {
        this.pairingParameters = pairingParameters;
        this.g1 = g1;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }

        GlobalParameters other = (GlobalParameters) obj;

        return Objects.deepEquals(new String[] {getPairingParameters(), getG1()},
                new String[] {other.getPairingParameters(), other.getG1()});
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPairingParameters(), getG1());
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "@" + Integer.toHexString(hashCode()) + " [pairingParameters=" +
                pairingParameters + ", g1=" + g1 + "]";
    }

}
