// Adds demo freelancers, a demo client and sample gigs for development and
// demos. Run from the backend folder after seedAdmin:
//   node utils/seedGigs.js
// Safe to run more than once, because anything that already exists is skipped.
// Needs DEMO_PASSWORD in .env. It must never be committed or hard-coded
// (OWASP, 2026e; IIE, 2026). All names and emails are made up.
const path = require("path");

// loads backend/.env no matter which folder the script is run from (IIE, 2026)
require("dotenv").config({ path: path.join(__dirname, "..", ".env"), quiet: true });

const mongoose = require("mongoose");
const User = require("../models/userModel.js");
const Gig = require("../models/gigModel.js");

// demo accounts, all using the same DEMO_PASSWORD from .env
const DEMO_USERS = [
    { key: "f1", username: "demo_freelancer_1", email: "demo.freelancer1@example.com", role: "freelancer" },
    { key: "f2", username: "demo_freelancer_2", email: "demo.freelancer2@example.com", role: "freelancer" },
    { key: "f3", username: "demo_freelancer_3", email: "demo.freelancer3@example.com", role: "freelancer" },
    { key: "c1", username: "demo_client_1", email: "demo.client1@example.com", role: "client" },
];

// one gig for each category in the Gig model, shared between the freelancers
const GIGS = [
    { owner: "f1", category: "Web Development", title: "Responsive portfolio website", price: 1500,
      description: "I will build a clean, mobile friendly portfolio website with up to five pages, a contact form and basic SEO set up." },
    { owner: "f1", category: "Programming", title: "Python script to automate your reports", price: 900,
      description: "I will write a Python script that collects your data, tidies it and produces a weekly report, with simple instructions to run it." },
    { owner: "f1", category: "Data Entry", title: "Accurate spreadsheet data capture", price: 350,
      description: "I will type up and check up to 500 records from scanned forms or PDFs into a clean spreadsheet within two days." },
    { owner: "f1", category: "Tutoring", title: "One hour beginner coding lesson", price: 250,
      description: "A friendly one hour online lesson covering the basics of HTML, CSS and JavaScript, with practice exercises to keep." },
    { owner: "f2", category: "Graphic Design", title: "Modern logo design with three concepts", price: 1200,
      description: "I will design three original logo concepts for your business and deliver the final files in PNG and SVG formats." },
    { owner: "f2", category: "Photography", title: "Product photography for online shops", price: 800,
      description: "I will photograph up to ten products on a clean background and edit them so they are ready to upload to your store." },
    { owner: "f2", category: "Video Editing", title: "Social media video edit", price: 650,
      description: "I will edit your footage into a polished clip of up to sixty seconds with captions, music and smooth transitions." },
    { owner: "f3", category: "Writing", title: "Blog articles that keep readers engaged", price: 450,
      description: "I will research and write a 800 word blog article in your tone of voice, with a clear structure and a short summary." },
    { owner: "f3", category: "Marketing", title: "Social media content plan for one month", price: 700,
      description: "I will plan thirty days of posts for your business, with caption ideas, hashtags and the best times to post." },
    { owner: "f3", category: "Music & Audio", title: "Podcast audio clean up", price: 550,
      description: "I will remove background noise, balance the volume and add intro music to up to thirty minutes of podcast audio." },
];

const seedGigs = async () => {
    const { DEMO_PASSWORD, CONN_STRING, NODE_ENV } = process.env;

    // demo data must never reach a live database
    if (NODE_ENV === "production") {
        console.error("Refusing to add demo data when NODE_ENV is production.");
        process.exit(1);
    }

    // says exactly which value is missing, without printing any secret (OWASP, 2026e)
    const missing = ["DEMO_PASSWORD", "CONN_STRING"].filter((key) => !process.env[key]);
    if (missing.length > 0) {
        console.error(`Missing in .env: ${missing.join(", ")}`);
        process.exit(1);
    }

    // same password rules as normal registration (OWASP, 2026a)
    if (DEMO_PASSWORD.length < 8 || !/\d/.test(DEMO_PASSWORD) || !/[A-Z]/.test(DEMO_PASSWORD)) {
        console.error("DEMO_PASSWORD must be at least 8 characters with a number and an uppercase letter.");
        process.exit(1);
    }

    // every gig category must exist in the model, so a typo is caught before anything is saved (IIE, 2026)
    const allowed = Gig.schema.path("category").enumValues;
    const badGig = GIGS.find((gig) => !allowed.includes(gig.category));
    if (badGig) {
        console.error(`"${badGig.category}" is not a category in the Gig model.`);
        process.exit(1);
    }

    try {
        // fails after 8 seconds instead of hanging if the database cannot be reached
        await mongoose.connect(CONN_STRING, { serverSelectionTimeoutMS: 8000 });

        // 1. demo users: created through User.create so the password is hashed (OWASP, 2026d)
        const userIds = {};
        for (const demo of DEMO_USERS) {
            let user = await User.findOne({ email: demo.email });
            if (user) {
                console.log(`Skipped user ${demo.username} (already exists).`);
            } else {
                user = await User.create({
                    username: demo.username,
                    email: demo.email,
                    password: DEMO_PASSWORD,
                    role: demo.role,
                });
                console.log(`Created ${demo.role} ${demo.username}.`);
            }
            userIds[demo.key] = user._id;
        }

        // 2. demo gigs: a gig is skipped if that freelancer already has one with the same title
        let created = 0;
        for (const gig of GIGS) {
            const exists = await Gig.findOne({ title: gig.title, freelancer: userIds[gig.owner] });
            if (exists) continue;

            await Gig.create({
                title: gig.title,
                description: gig.description,
                category: gig.category,
                price: gig.price,
                freelancer: userIds[gig.owner],
            });
            created++;
        }
        console.log(`Gigs created: ${created}, skipped: ${GIGS.length - created}.`);
    } catch (error) {
        // only the message is printed, never the connection string (OWASP, 2026e)
        console.error("[seedGigs] error:", error.message);
        process.exitCode = 1;
    } finally {
        await mongoose.disconnect();
    }
};

seedGigs();

/*
References:
The Independent Institute of Education (IIE), 2026. Information Systems 3D [INSY7314/W Module Manual] The Independent Institute of Education: Unpublished.
OWASP, 2026a. Authentication Cheat Sheet. [online] Available at: <https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html> [Accessed 4 September 2026].
OWASP, 2026d. Password Storage Cheat Sheet. [online] Available at: <https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html> [Accessed 4 September 2026].
OWASP, 2026e. REST Security Cheat Sheet. [online] Available at: <https://cheatsheetseries.owasp.org/cheatsheets/REST_Security_Cheat_Sheet.html> [Accessed 4 September 2026].
*/