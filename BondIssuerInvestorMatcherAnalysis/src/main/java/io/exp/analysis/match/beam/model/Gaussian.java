package io.exp.analysis.match.beam.model;

import lombok.Getter;

import java.io.Serializable;


@Getter
public class Gaussian implements Serializable {
    private double mean;
    private double var;
    private double std;
    private final static double FLOOR_CONSTANT=0.0000000001;
    private final static double constant2pi = Math.sqrt(2.0*Math.PI);
    Gaussian(double mean, double var, double std) {
        this.mean = mean;
        this.var = var;
        this.std = std;
    }

    public static GaussianBuilder builder() {
        return new GaussianBuilder();
    }

    public double pdf(double x){
        double p = 0;

        p = (x - mean);
        p = p*p/(var+FLOOR_CONSTANT);
        p = Math.exp (p * (-0.5));
        p = p /(this.std*constant2pi);
        return p;
    }

    public static class GaussianBuilder {
        private double mean;
        private double var;
        private double std;

        GaussianBuilder() {
        }

        public GaussianBuilder mean(double mean) {
            this.mean = mean;
            return this;
        }

        public GaussianBuilder var(double var) {
            this.var = var;
            this.std = Math.sqrt(var);
            return this;
        }

        public Gaussian build() {
            return new Gaussian(mean, var, std);
        }

        public String toString() {
            return "Gaussian.GaussianBuilder(mean=" + this.mean + ", var=" + this.var + ", std=" + this.std + ")";
        }
    }
}
