exports.needlemanWunsch = function (s1, s2) {
    const sp = 1; // Match score
    const gp = -1; // Gap penalty
    const gc = "-"; // Gap character
  
    // Initialize scoring matrix
    const arr = Array(s2.length + 1)
        .fill()
        .map(() => Array(s1.length + 1).fill(0) );
  
    for (let i = 1; i <= s2.length; i++) {
        arr[i][0] = gp * i;
    }
    for (let j = 1; j <= s1.length; j++) {
        arr[0][j] = gp * j;
    }
  
    // Fill the scoring matrix
    for (let i = 1; i <= s2.length; i++) {
        for (let j = 1; j <= s1.length; j++) {
            arr[i][j] = Math.max(
                arr[i - 1][j - 1] + (s2[i - 1] === s1[j - 1] ? sp : gp), // Match/Mismatch
                arr[i - 1][j] + gp, // Deletion
                arr[i][j - 1] + gp // Insertion
            );
        }
    }
  
    // Traceback to get the alignment
    let as1 = "";
    let as2 = "";
    let i = s2.length;
    let j = s1.length;
  
    while (i > 0 || j > 0) {
        if (i > 0 && j > 0 && arr[i][j] === arr[i - 1][j - 1] + (s2[i - 1] === s1[j - 1] ? sp : gp)) {
            as1 += s1[j - 1];
            as2 += s2[i - 1];
            i--;
            j--;
        } else if (i > 0 && arr[i][j] === arr[i - 1][j] + gp) {
            as1 += gc;
            as2 += s2[i - 1];
            i--;
        } else if (j > 0 && arr[i][j] === arr[i][j - 1] + gp) {
            as1 += s1[j - 1];
            as2 += gc;
            j--;
        }
    }
  
    // Reverse the sequences as they were built backwards
    as1 = as1.split("").reverse().join("");
    as2 = as2.split("").reverse().join("");
  
    return { s1, s2, as1, as2 };
};
  