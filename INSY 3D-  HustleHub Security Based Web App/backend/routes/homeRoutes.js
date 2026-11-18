//import express
const express = require("express");

// because this is home router, we call in the home controller
const {healthCheck, greet, message} = require("../controllers/homeController.js");

//set up an instance of router of the router class
const router = express.Router();

//each request requires a method, path and a cooresponding function in the controller

// for this request, we define the following
//method - get request
//path - what url we need to go to trigger this request
//function - we say that when this request is triggered, run the health check fuction in controller 
router.get("/healthCheck", healthCheck);

router.post("/greet", greet);

router.post("/message", message);

//this is the same as making something
module.exports = router;