const mongoose = require("mongoose");
const Schema = mongoose.Schema;

// Define the Alignment schema
const AlignmentSchema = new Schema({
    s1: {
        type: String,
        required: [true, "Sequence 1 is required"],
    },
    s2: {
        type: String,
        required: [true, "Sequence 2 is required"],
    },
    as1: {
        type: String,
        required: [true, "Aligned Sequence 1 is required"],
    },
    as2: {
        type: String,
        required: [true, "Aligned Sequence 2 is required"],
    },
});

// Create and export the Alignment model
module.exports = mongoose.model("Alignment", AlignmentSchema);
