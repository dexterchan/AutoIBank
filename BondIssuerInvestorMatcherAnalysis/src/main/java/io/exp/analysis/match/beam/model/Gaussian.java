package io.exp.analysis.match.beam.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Gaussian implements Serializable {
    private double mean;
    private double var;
}
