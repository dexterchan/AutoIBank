package io.exp.analysis.beam.utils.redis;

import lombok.Data;

import java.io.Serializable;
@Data
public class BytesContainer implements Serializable {
    byte[] data;
    public static BytesContainer of (byte[] data){
        BytesContainer c = new BytesContainer();
        c.setData(data);
        return c;
    }
}
