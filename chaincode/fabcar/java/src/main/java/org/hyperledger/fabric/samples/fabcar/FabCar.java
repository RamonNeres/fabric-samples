/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.samples.fabcar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contact;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.License;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;

import com.owlike.genson.Genson;

/**
 * Java implementation of the Fabric Car Contract described in the Writing Your
 * First Application tutorial
 */
@Contract(
        name = "MedRecord",
        info = @Info(
                title = "MedRecord contract",
                description = "The hyperlegendary car contract",
                version = "0.0.1-SNAPSHOT",
                license = @License(
                        name = "Apache 2.0 License",
                        url = "http://www.apache.org/licenses/LICENSE-2.0.html"),
                contact = @Contact(
                        email = "f.carr@example.com",
                        name = "F Carr",
                        url = "https://hyperledger.example.com")))
@Default
public final class FabCar implements ContractInterface {

    private final Genson genson = new Genson();

    private enum FabCarErrors {
        CAR_NOT_FOUND,
        CAR_ALREADY_EXISTS
    }

    @Transaction()
    public MedRecord queryMedRecord(final Context ctx, final String key) {
        ChaincodeStub stub = ctx.getStub();
        String medRecordState = stub.getStringState(key);

        if (medRecordState.isEmpty()) {
            String errorMessage = String.format("MedRecord %s does not exist", key);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, FabCarErrors.CAR_NOT_FOUND.toString());
        }

        return genson.deserialize(medRecordState, MedRecord.class);
    }

    @Transaction()
    public String queryMedRecordFile(final Context ctx, final String key) {
        ChaincodeStub stub = ctx.getStub();
        String medRecordState = stub.getStringState(key);

        if (medRecordState.isEmpty()) {
            String errorMessage = String.format("MedRecord %s does not exist", key);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, FabCarErrors.CAR_NOT_FOUND.toString());
        }

        MedRecord medRecord = genson.deserialize(medRecordState, MedRecord.class);
        String path = medRecord.getBucket();

        try{
            return Util.encodeFileToBase64Binary(path);
        } catch (IOException ex) {
            throw new ChaincodeException(ex.getMessage(), "Erro ao ler o arquivo");
        }
    }

    @Transaction()
    public Authority queryAuthority(final Context ctx, final String key) {
        ChaincodeStub stub = ctx.getStub();
        String medRecordState = stub.getStringState(key);

        if (medRecordState.isEmpty()) {
            String errorMessage = String.format("Authority %s does not exist", key);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, FabCarErrors.CAR_NOT_FOUND.toString());
        }

        return genson.deserialize(medRecordState, Authority.class);
    }

    @Transaction()
    public GlobalParameters queryGlobalParameter(final Context ctx, final String key) {
        ChaincodeStub stub = ctx.getStub();
        String medRecordState = stub.getStringState(key);

        if (medRecordState.isEmpty()) {
            String errorMessage = String.format("GP %s does not exist", key);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, FabCarErrors.CAR_NOT_FOUND.toString());
        }

        GlobalParameters gp = genson.deserialize(medRecordState, GlobalParameters.class);

        return gp;
    }

    //TODO: Deve receber userKey, authorityKey e gpKey, para verificar se existem
    @Transaction()
    public MedRecord createMedRecord(final Context ctx, final String key, final String userId, final String bucket,
                                     final String authorityID, final String globalParID, final String base64RecordFile) {
        ChaincodeStub stub = ctx.getStub();

        String medRecState = stub.getStringState(key);
        if (!medRecState.isEmpty()) {
            String errorMessage = String.format("MedRecord %s already exists", key);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, FabCarErrors.CAR_ALREADY_EXISTS.toString());
        }

        MedRecord medRecord = new MedRecord(userId, bucket, authorityID, globalParID);
        medRecState = genson.serialize(medRecord);
        stub.putStringState(key, medRecState);

        try {
            Util.base64toFile(userId, base64RecordFile);
        } catch (IOException ex) {
            throw new ChaincodeException(ex.getMessage(), "Erro ao gravar o arquivo");
        }

        return medRecord;
    }

    @Transaction()
    public User createUser(final Context ctx, final String key, final String userId, final String attributes) {
        ChaincodeStub stub = ctx.getStub();

        String userState = stub.getStringState(key);
        if (!userState.isEmpty()) {
            String errorMessage = String.format("User %s already exists", key);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, FabCarErrors.CAR_ALREADY_EXISTS.toString());
        }

        User user = new User(userId, attributes.split(","));
        userState = genson.serialize(user);
        stub.putStringState(key, userState);

        return user;
    }

    @Transaction()
    public GlobalParameters createGlobalParameter(final Context ctx, final String key, final String pairingParameters, final String g1) {
        ChaincodeStub stub = ctx.getStub();

        String gpState = stub.getStringState(key);
        if (!gpState.isEmpty()) {
            String errorMessage = String.format("GlobalParameter %s already exists", key);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, FabCarErrors.CAR_ALREADY_EXISTS.toString());
        }

        GlobalParameters gp = new GlobalParameters(pairingParameters, g1);
        gpState = genson.serialize(gp);
        stub.putStringState(key, gpState);

        return gp;
    }

    @Transaction()
    public Authority createAuthority(final Context ctx, final String key, final String name, final String policy,
                                            final String eg1g1a1, final String g1yi) {
        ChaincodeStub stub = ctx.getStub();

        String authorityState = stub.getStringState(key);

        if (!authorityState.isEmpty()) {
            String errorMessage = String.format("Authority %s already exists", key);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, FabCarErrors.CAR_ALREADY_EXISTS.toString());
        }

        Authority authority = new Authority(name, policy, eg1g1a1, g1yi);
        authorityState = genson.serialize(authority);
        stub.putStringState(key, authorityState);

        return authority;
    }
    @Transaction
    public Authority addPublicKey(final Context ctx, final String key, final String policy, final String eg1g1a1, final String g1yi) {
        ChaincodeStub stub = ctx.getStub();

        String authorityState = stub.getStringState(key);

        if (authorityState.isEmpty()) {
            String errorMessage = String.format("Authority %s does not exist", key);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, FabCarErrors.CAR_NOT_FOUND.toString());
        }

        Authority authority = genson.deserialize(authorityState, Authority.class);
        if(authority.getPublicKeys().containsKey(policy))
            throw new ChaincodeException("Policy já existe nessa Authority", FabCarErrors.CAR_ALREADY_EXISTS.toString());

        Authority newAuthority = new Authority(authority.getAuthorityID());
        authority.getPublicKeys().forEach((polName, polKeys) -> {
            newAuthority.addPublicKey(polName, polKeys.getEg1g1a1(), polKeys.getG1yi());
        });

        newAuthority.addPublicKey(policy, eg1g1a1, g1yi);

        String newAuthorityState = genson.serialize(newAuthority);
        stub.putStringState(key, newAuthorityState);

        return newAuthority;
    }
    
    /* FABCAR */

    /**
     * Retrieves a car with the specified key from the ledger.
     *
     * @param ctx the transaction context
     * @param key the key
     * @return the Car found on the ledger if there was one
     */
    @Transaction()
    public Car queryCar(final Context ctx, final String key) {
        ChaincodeStub stub = ctx.getStub();
        String carState = stub.getStringState(key);

        if (carState.isEmpty()) {
            String errorMessage = String.format("Car %s does not exist", key);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, FabCarErrors.CAR_NOT_FOUND.toString());
        }

        Car car = genson.deserialize(carState, Car.class);

        return car;
    }

    /**
     * Creates some initial Cars on the ledger.
     *
     * @param ctx the transaction context
     */
    @Transaction()
    public void initLedger(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();

        String[] carData = {
                "{ \"make\": \"Toyota\", \"model\": \"Prius\", \"color\": \"blue\", \"owner\": \"Tomoko\" }",
                "{ \"make\": \"Ford\", \"model\": \"Mustang\", \"color\": \"red\", \"owner\": \"Brad\" }",
                "{ \"make\": \"Hyundai\", \"model\": \"Tucson\", \"color\": \"green\", \"owner\": \"Jin Soo\" }",
                "{ \"make\": \"Volkswagen\", \"model\": \"Passat\", \"color\": \"yellow\", \"owner\": \"Max\" }",
                "{ \"make\": \"Tesla\", \"model\": \"S\", \"color\": \"black\", \"owner\": \"Adrian\" }",
                "{ \"make\": \"Peugeot\", \"model\": \"205\", \"color\": \"purple\", \"owner\": \"Michel\" }",
                "{ \"make\": \"Chery\", \"model\": \"S22L\", \"color\": \"white\", \"owner\": \"Aarav\" }",
                "{ \"make\": \"Fiat\", \"model\": \"Punto\", \"color\": \"violet\", \"owner\": \"Pari\" }",
                "{ \"make\": \"Tata\", \"model\": \"nano\", \"color\": \"indigo\", \"owner\": \"Valeria\" }",
                "{ \"make\": \"Holden\", \"model\": \"Barina\", \"color\": \"brown\", \"owner\": \"Shotaro\" }"
        };

        for (int i = 0; i < carData.length; i++) {
            String key = String.format("CAR%03d", i);

            Car car = genson.deserialize(carData[i], Car.class);
            String carState = genson.serialize(car);
            stub.putStringState(key, carState);
        }
    }

    /**
     * Creates a new car on the ledger.
     *
     * @param ctx the transaction context
     * @param key the key for the new car
     * @param make the make of the new car
     * @param model the model of the new car
     * @param color the color of the new car
     * @param owner the owner of the new car
     * @return the created Car
     */
    @Transaction()
    public Car createCar(final Context ctx, final String key, final String make, final String model,
                         final String color, final String owner) {
        ChaincodeStub stub = ctx.getStub();

        String carState = stub.getStringState(key);
        if (!carState.isEmpty()) {
            String errorMessage = String.format("Car %s already exists", key);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, FabCarErrors.CAR_ALREADY_EXISTS.toString());
        }

        Car car = new Car(make, model, color, owner);
        carState = genson.serialize(car);
        stub.putStringState(key, carState);

        return car;
    }

    /**
     * Retrieves every car between CAR0 and CAR999 from the ledger.
     *
     * @param ctx the transaction context
     * @return array of Cars found on the ledger
     */
    @Transaction()
    public Car[] queryAllCars(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();

        final String startKey = "CAR0";
        final String endKey = "CAR999";
        List<Car> cars = new ArrayList<Car>();

        QueryResultsIterator<KeyValue> results = stub.getStateByRange(startKey, endKey);

        for (KeyValue result: results) {
            Car car = genson.deserialize(result.getStringValue(), Car.class);
            cars.add(car);
        }

        Car[] response = cars.toArray(new Car[cars.size()]);

        return response;
    }

    /**
     * Changes the owner of a car on the ledger.
     *
     * @param ctx the transaction context
     * @param key the key
     * @param newOwner the new owner
     * @return the updated Car
     */
    @Transaction()
    public Car changeCarOwner(final Context ctx, final String key, final String newOwner) {
        ChaincodeStub stub = ctx.getStub();

        String carState = stub.getStringState(key);

        if (carState.isEmpty()) {
            String errorMessage = String.format("Car %s does not exist", key);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, FabCarErrors.CAR_NOT_FOUND.toString());
        }

        Car car = genson.deserialize(carState, Car.class);

        Car newCar = new Car(car.getMake(), car.getModel(), car.getColor(), newOwner);
        String newCarState = genson.serialize(newCar);
        stub.putStringState(key, newCarState);

        return newCar;
    }
}
