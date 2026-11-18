const mongoose = require('mongoose');

const bcrypt = require("bcryptjs");

const twoFactorSchema = new mongoose.Schema({
    enabled: { type: Boolean, default: false },
    secret: String,
    pendingSecret: String,
    recoveryCodes: [String],
    lastUsedStep: Number,
    failedAttempts: { type: Number, default: 0 },
    lockedUntil: Date,
}, { _id: false });

const userSchema = new mongoose.Schema({
    username: {
        type: String,
        required: true,
        trim: true,
        // --- added: schema-level length constraints (backstop if a controller check is skipped) ---
        minlength: [3, "Username must be at least 3 characters"],
        maxlength: [30, "Username must be at most 30 characters"],
    },
    email: {
        type: String,
        required: true,
        unique: true,
        lowercase: true,
        trim: true,
        // --- added: schema-level format check ---
        match: [/^[^\s@]+@[^\s@]+\.[^\s@]+$/, "Please enter a valid email address"],
    },
    password: {
        type: String,
        required: true,
        // schema-level minimum length: runs against the plaintext password,
        // since Mongoose validation happens before the pre("save") hashing
        // hook below fires.
        minlength: [8, "Password must be at least 8 characters"],
    },
    role:{
        type: String,
        enum: ["client", "freelancer", "admin"],
        default: "client",
    },
    tokenVersion: { type: Number, default: 0 },
        twoFactor: { 
            type: twoFactorSchema,
             select: false, 
             default: () => ({}) },
});

//hook function -> is something that runs when something else is called
//when we save the user -> a hook function will run to do something before the user is saved
userSchema.pre("save", async function (){
    if (!this.isModified("password")){
        return ;
    }
    try{
        //1 => season the password with some salt
        const salt = await bcrypt.genSalt(12);
        // 2 => we combine salt with the users password
        this.password = await bcrypt.hash(this.password, salt);
        //3 => once the password is appropriately seasoned, we move to the next step, which is saving
        ;
    } catch (error){
        console.log(error.message);
    }
});

//write a helper function to compare the user input when logging in (which in plain text), with seasoned password
//in our database 
userSchema.methods.matchPassword = async function (enteredPassword) {
    //uses the compare method to comopare (wow)
    return await bcrypt.compare(enteredPassword, this.password);
}

//export the model so that it can be used elsewhere :)
module.exports = mongoose.model("User", userSchema);
