//call required modules
const mongoose = require('mongoose');

//calls .evn file
require('dotenv').config();

const database = {
    start:async() => {
        try{
            const connString = process.env.CONN_STRING;

        await mongoose.connect(connString);

        console.log("The database connection has connected");

        
    }
    catch (error){
        console.log("Cannot connect to the database" + error);
        // if we cant use db 
        //we exit
        process.exit(1);
    }
    },
};

module.exports = database;

