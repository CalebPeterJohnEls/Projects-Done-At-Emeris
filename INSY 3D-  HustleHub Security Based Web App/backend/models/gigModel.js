// mongoose also allows us to define models, which can be thought of as "tables".
// they allow us to have a rigid structure in a document database, which often expects unstructured data

const mongoose = require("mongoose");

// we start by creating a schema, which is a map/layout of what we want the model to store

const gigSchema = new mongoose.Schema(
    {
        // we don't create a primary key here - as mongo automatically assigns a unique ID to each entry

        title: {
            type: String,
            required: true,
            trim: true,
            maxlength: 100,
        },

        description: {
            type: String,
            required: true,
            trim: true,
            maxlength: 2000,
        },

        category: {
            type: String,
            // by using an enum, you can restrict what the user can enter to specific strings
            enum: [
                "Web Development",
                "Graphic Design",
                "Writing",
                "Photography",
                "Video Editing",
                "Marketing",
                "Programming",
                "Music & Audio",
                "Tutoring",
                "Data Entry"
            ],
            required: true,
            trim: true,
        },

        price: {
            type: Number,
            required: true,
            min: 0,
        },

        // Links the gig to the freelancer who created it - this is what
        // makes ownership checks in the controller possible. Without it,
        // any freelancer could edit or delete any other freelancer's gig.
        freelancer: {
            type: mongoose.Schema.Types.ObjectId,
            ref: "User",
            required: true,
        },

        // Lets a freelancer pause a gig without deleting it outright -
        // useful once bookings reference gigs, since you don't want to
        // hard-delete a gig that has booking history attached to it.
        isActive: {
            type: Boolean,
            default: true,
        },
    },

    {
        // by enabling timestamps in the schema, the database will automatically create 2 additional helpful fields
        // createdAt - when the object was added to the database
        // updatedAt - when the object was last updated or changed
        timestamps: true,
    },
);

// once we've created the schema, we convert it into a model, so that it can be used elsewhere in the app
// think of the schema as what the table looks like, and the model as the actual interface to the table

const Gig = mongoose.model("Gig", gigSchema);

// export all the methods to make them "public" (accessible in other files)
module.exports = Gig;