/*
 * SPDX-License-Identifier: Apache-2.0
 */

'use strict';

const { Contract } = require('fabric-contract-api');

class FabCar extends Contract {

    async initLedger(ctx) {
        console.info('============= START : Initialize Ledger ===========');
        // const cars = [
        //     {
        //         color: 'blue',
        //         make: 'Toyota',
        //         model: 'Prius',
        //         owner: 'Tomoko',
        //         year: 2010,
        //     },
        //     {
        //         color: 'red',
        //         make: 'Ford',
        //         model: 'Mustang',
        //         owner: 'Brad',
        //         year: 2012,
        //     },
        //     {
        //         color: 'green',
        //         make: 'Hyundai',
        //         model: 'Tucson',
        //         owner: 'Jin Soo',
        //         year: 2014,
        //     },
        //     {
        //         color: 'yellow',
        //         make: 'Volkswagen',
        //         model: 'Passat',
        //         owner: 'Max',
        //         year: 2008,
        //     },
        //     {
        //         color: 'black',
        //         make: 'Tesla',
        //         model: 'S',
        //         owner: 'Adriana',
        //         year: 2020,
        //     },
        //     {
        //         color: 'purple',
        //         make: 'Peugeot',
        //         model: '205',
        //         owner: 'Michel',
        //         year: 2009,
        //     },
        //     {
        //         color: 'white',
        //         make: 'Chery',
        //         model: 'S22L',
        //         owner: 'Aarav',
        //         year: 2018,
        //     },
        //     {
        //         color: 'violet',
        //         make: 'Fiat',
        //         model: 'Punto',
        //         owner: 'Pari',
        //         year: 2016,
        //     },
        //     {
        //         color: 'indigo',
        //         make: 'Tata',
        //         model: 'Nano',
        //         owner: 'Valeria',
        //         year: 2014,
        //     },
        //     {
        //         color: 'brown',
        //         make: 'Holden',
        //         model: 'Barina',
        //         owner: 'Shotaro',
        //         year: 2011,
        //     },
        // ];

        // for (let i = 0; i < cars.length; i++) {
        //     cars[i].docType = 'car';
        //     await ctx.stub.putState('CAR' + i, Buffer.from(JSON.stringify(cars[i])));
        //     console.info('Added <--> ', cars[i]);
        // }
        console.info('============= END : Initialize Ledger ===========');
    }

    async queryCar(ctx, carNumber) {
        const carAsBytes = await ctx.stub.getState(carNumber); // get the car from chaincode state
        if (!carAsBytes || carAsBytes.length === 0) {
            throw new Error(`${carNumber} does not exist`);
        }
        console.log(carAsBytes.toString());
        return carAsBytes.toString();
    }

    async createCar(ctx, carNumber, make, model, color, owner, year) {
        console.info('============= START : Create Car ===========');

        const car = {
            color,
            docType: 'car',
            make,
            model,
            owner,
            year,
        };

        await ctx.stub.putState(carNumber, Buffer.from(JSON.stringify(car)));
        console.info('============= END : Create Car ===========');
    }

    async queryAllCars(ctx) {
        const startKey = 'CAR0';
        const endKey = 'CAR999';
        const allResults = [];
        for await (const {key, value} of ctx.stub.getStateByRange(startKey, endKey)) {
            const strValue = Buffer.from(value).toString('utf8');
            let record;
            try {
                record = JSON.parse(strValue);
            } catch (err) {
                console.log(err);
                record = strValue;
            }
            allResults.push({ Key: key, Record: record });
        }
        console.info(allResults);
        return JSON.stringify(allResults);
    }

    async changeCarOwner(ctx, carNumber, newOwner) {
        console.info('============= START : changeCarOwner ===========');

        const carAsBytes = await ctx.stub.getState(carNumber); // get the car from chaincode state
        if (!carAsBytes || carAsBytes.length === 0) {
            throw new Error(`${carNumber} does not exist`);
        }
        const car = JSON.parse(carAsBytes.toString());
        car.owner = newOwner;

        await ctx.stub.putState(carNumber, Buffer.from(JSON.stringify(car)));
        console.info('============= END : changeCarOwner ===========');
    }

}

module.exports = FabCar;
