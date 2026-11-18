// Creates the first admin account. Run once from the project root
// Credentials come from .env and must never be committed or hard-coded:
//   ADMIN_USERNAME, ADMIN_EMAIL, ADMIN_PASSWORD (and your existing DB URI variable)
require("dotenv").config();
const mongoose = require("mongoose");
const User = require("../models/userModel.js");

const seedAdmin = async () => {
    const { ADMIN_USERNAME, ADMIN_EMAIL, ADMIN_PASSWORD } = process.env;
    // TODO: use whatever variable name app.js reads for the connection string
    const dbUri = process.env.CONN_STRING;

    if (!ADMIN_USERNAME || !ADMIN_EMAIL || !ADMIN_PASSWORD || !dbUri) {
        console.error("Missing ADMIN_USERNAME, ADMIN_EMAIL, ADMIN_PASSWORD or the DB URI in .env");
        process.exit(1);
    }

    try {
        await mongoose.connect(dbUri);

        const existing = await User.findOne({ email: ADMIN_EMAIL.toLowerCase() });
        if (existing) {
            console.log("An account with that email already exists. Nothing changed.");
        } else {
            // User.create runs the pre-save hook, so the password is hashed
            await User.create({
                username: ADMIN_USERNAME,
                email: ADMIN_EMAIL,
                password: ADMIN_PASSWORD,
                role: "admin",
            });
            console.log("Admin account created.");
        }
    } catch (error) {
        console.error("[seedAdmin] error:", error.message);
        process.exitCode = 1;
    } finally {
        await mongoose.disconnect();
    }
};

seedAdmin();