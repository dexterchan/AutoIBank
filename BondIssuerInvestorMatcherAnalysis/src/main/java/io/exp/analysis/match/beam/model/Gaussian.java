package io.exp.analysis.match.beam.model;

import lombok.*;

import java.io.Serializable;


@Getter
@Builder
public class Gaussian implements Serializable {
    private double mean;
    private double var;
}
