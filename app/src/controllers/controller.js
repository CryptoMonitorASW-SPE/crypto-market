const mongoose = require("mongoose");
const Alignment = require("../models/alignmentModels");
const sa = require("../lib/sequences_alignment");

// Handle alignment submission
exports.alignSequences = async (req, res) => {
    const { seq1, seq2 } = req.body;

    // Validate input
    if (!seq1 || !seq2) {
        return res.status(400).json({
            description: "Two sequences seq1 and seq2 are required",
        });
    }

    try {
    // Check if alignment exists in the database
        let alignment = await Alignment.findOne({ s1: seq1, s2: seq2 }, "-_id -__v");

        if (!alignment) {
            // Calculate alignment using Needleman-Wunsch algorithm
            alignment = sa.needleman_wunsch(seq1, seq2);

            // Save the new alignment to the database
            const newAlignment = new Alignment(alignment);
            await newAlignment.save();
        }

        // Respond with the alignment result
        res.json(alignment);
    } catch (err) {
        console.error("Error in align_sequences:", err);
        res.status(500).json({ description: "Internal Server Error" });
    }
};

// Retrieve all alignments
exports.getAllAlignSequences = async (req, res) => {
    try {
        const alignments = await Alignment.find({}, "-_id -__v");
        res.json(alignments);
    } catch (err) {
        console.error("Error in get_all_align_sequences:", err);
        res.status(500).json({ description: "Internal Server Error" });
    }
};
