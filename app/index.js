const express = require("express");
const mongoose = require("mongoose");
const cors = require("cors"); // Enables CORS
const bodyParser = require("body-parser");

const routes = require("./src/routes/routes");

// Create Express app
const app = express();

// Middleware
app.use(cors()); // Enable CORS for all origins
app.use(bodyParser.urlencoded({ extended: true }));
app.use(bodyParser.json());

// Connect to MongoDB
const mongoURI = process.env.MONGO_URI || "mongodb://mongodb:27017/dbsa";

mongoose.set("strictQuery", false); // Optional: Adjust based on Mongoose version
mongoose
    .connect(mongoURI, { useNewUrlParser: true, useUnifiedTopology: true })
    .then(() => console.log("✅ MongoDB Connected!"))
    .catch((err) => console.error("❌ MongoDB Connection Error:", err));

// Health Check Endpoint
app.get("/health", (req, res) => {
    res.status(200).send("OK");
});

// Use routes
app.use("/api", routes);


// Start the server
const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
    console.log(`🚀 Node API server started on port ${PORT}!`);
});
