Project Title:
Mini C Obfuscator

Description:
This project implements a code obfuscator for a simplified version of the C programming language called Mini C. The tool reads Mini C source code, parses it, and outputs a functionally equivalent but obfuscated version.

How to Run:
1. Compile the project

2.Place your Mini C input in a file named input.mc

3.Run the program

4.The obfuscated output will be written to output.mc
   
Obfuscation Techniques Applied:

Variable name obfuscation (e.g. x → a0)

Expression transformation (e.g. x + y → x - (-y))

Dead code insertion (e.g. if (0) { int dummy = 0; })

How We Test Functional Equivalence:

To ensure that the obfuscated code behaves exactly like the original:

We compile and execute both versions of the code.

We use sample inputs to verify identical outputs.

Screenshots of both runs are included in the report for proof.




