const express = require("express");
const router = express.Router();

const controller = require("../controllers/controller");

// API Routes
router.post("/submit", controller.alignSequences);
router.get("/show", controller.getAllAlignSequences);

module.exports = router;
