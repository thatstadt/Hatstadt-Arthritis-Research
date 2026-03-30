# Stochastic Simulation Modeling of Long-Latency Risk in Young-Onset Rheumatoid Arthritis


> **Key Idea:** Population-level disease dynamics can be reproduced by simulating structured individual-level risk using stochastic, Monte Carlo–based models.

---

> **What’s Novel:** This work models long-latency risk by explicitly perturbing population-level exposure distributions and evaluating their effects through stochastic simulation, enabling counterfactual analysis that is difficult to achieve with observational data alone.

---
 
## Overview

This repository implements a stochastic, Monte Carlo–based simulation framework for modeling rheumatoid arthritis risk at the population level. The model generates large synthetic populations and assigns disease outcomes probabilistically using parameterized risk functions derived from epidemiological data. It is designed to evaluate how individual-level risk factors scale into population-level disease patterns under varying epidemiological assumptions.

A central focus is modeling long-latency exposure effects by perturbing BMI distributions across age groups and analyzing their impact under counterfactual scenarios. Simulated outputs are compared against Global Burden of Disease estimates to assess consistency with observed prevalence patterns.

---

## Method Summary

Each simulation generates a large synthetic population and assigns disease outcomes probabilistically using parameterized risk functions.

Individual risk is modeled as a function of baseline prevalence, BMI-dependent modifiers, and age-dependent attenuation to reflect long-latency exposure effects. Monte Carlo sampling is used to assign outcomes across individuals, and results are aggregated over repeated trials to estimate prevalence distributions.

The framework enables systematic perturbation of BMI distributions across age groups, allowing for counterfactual analysis of how changes in population-level risk factors influence disease prevalence.

---

## Scale

Each simulation run generates a synthetic population of approximately 1,000,000 individuals. Results are aggregated over ~2,500 Monte Carlo trials to estimate prevalence distributions and assess variability under different parameter configurations.

The simulation is implemented in Java, with downstream analysis and visualization conducted in Python using Jupyter notebooks.

---

## Repository Structure

The repository is organized into simulation, analysis, and output components:

- `Java/Algo.java` — core simulation engine implementing population generation, parameterized risk functions, and Monte Carlo outcome assignment  
- `Python/ra_all.ipynb` — primary analysis notebook for processing simulation outputs and computing prevalence statistics  
- `Python/Graphs.ipynb` — visualization of simulation results and parameter sensitivity analyses  
- `Data/` — CSV outputs generated from simulation runs, including parameter sweeps and aggregated results  
- `Graphs/` — figures produced from analysis, including distributions and comparative plots used for interpretation  

---

## How to Read This Repository

For a quick overview of the project:

1. Review the **Overview** and **Method Summary** sections
2. Inspect the figures in `Graphs/` to understand simulation outputs
3. Review `Java/Algo.java` for core simulation logic
4. Review the notebooks in `Python/` for downstream analysis and visualization

This repository is structured to support both high-level interpretation of results and inspection of the underlying simulation framework.

---

## Validation and Scope

This model is not intended to establish causal relationships, but rather to evaluate whether structured representations of known risk factors can reproduce observed population-level disease patterns under realistic assumptions.

Simulated outputs are compared against Global Burden of Disease prevalence estimates to ensure results remain within clinically realistic ranges.

The framework is primarily exploratory and methodological, focusing on how long-latency exposure effects can be represented and evaluated using stochastic simulation.

---

## Related Materials

- **Preprint (bioRxiv):** [View paper](https://doi.org/10.64898/2026.01.29.702692)
- **AAI Midwinter Conference Poster:** [View poster](https://osf.io/7my48/overview)
- **Whiteboard Explanation Video:** [Watch video](https://www.youtube.com/watch?v=VnPhs0IpSGY)

These materials provide additional context on the modeling framework, experimental design, and interpretation of results.

---

## Project Status

This repository reflects an active research workflow rather than a finalized software release. The codebase is structured to support iterative simulation, analysis, and visualization of epidemiological scenarios.

---

## Authorship

All simulation code, analysis, and visualization in this repository were developed by Tyler Hatstadt in support of the associated preprint and conference presentation.
