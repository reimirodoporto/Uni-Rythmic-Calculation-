# Uni-Rythmics: Horological Metrology & Scaling Workbench (Android)

An interactive Android (Kotlin + Jetpack Compose + Material 3) visual metrology engine and learning instrument styled with an Edison vacuum-glass chamber, electric-cyan EL-wire edge piping, coupled skeletal brass clockwork gears, and watch-style 6-digit rolodex numerical drums.

## Core Capabilities

- **3-Level Comparison Chain (`Workbench`)**:
  - **Level 1 (`1 Gunit`)**: Interactive 360° 12-sector Dodecagonal Dial (`12 × 30°`), Half vs. Whole Measure toggles, Exact (`1/Φ`) vs. Rounded (`0.618`) precision toggles, and $n$-th power decimal shifts ($10^0 \to 10^{-9}$) with live `1/1`, `1/10`, and sector-weighted `1 Gunit` readouts.
  - **Level 2 (`1 var` + `×000001` Rolodex + Final Total)**: Scientific variable selector across SI Base Quantities, Planck quantum scales, and CODATA 2022 constants, paired with a 6-digit mechanical rolodex integer multiplier (`000001`–`999999`), Final Scaled Total, and 5-step arithmetic proof.
  - **Level 3 (`var of comparison`)**: Collapsible/toggleable comparison stage supporting **Notable Query** (searchable CODATA 2022 benchmarks) and **Secondary Var** (linked or independent side-by-side unit scaling).
- **4-Basis Scale Matrix (`Scale Matrix`)**:
  - Side-by-side comparison of `Half Rounded`, `Half Exact`, `Whole Rounded`, and `Whole Exact` with Exact vs. `0.618` divergence readouts and logarithmic magnitude bars.
- **Scientific Measurement Taxonomies (`Taxonomies`)**:
  - Interactive reference across the 7 SI Base Quantities, Planck Quantum Scales, Stevens' 4 Levels of Scale, and Scalars/Vectors/Tensors with one-click loading into `1 var` or `var of comparison`.
- **Interactive Calibration Challenges (`Challenges`)**:
  - Progressive metrology targets verified live against the active Workbench calibration.
- **Gemini AI Assist (`AI Assist`)**:
  - Powered by Firebase AI Logic (`gemini-3.8-flash`) with App Check attestation for streaming Q&A and structured JSON metrology breakdowns grounded in live Workbench state.
