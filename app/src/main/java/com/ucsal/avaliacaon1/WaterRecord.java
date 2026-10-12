
package com.ucsal.avaliacaon1;

public class WaterRecord {

    private final int volumeMl;
    private final String dataHora;
    private final String data;

    public WaterRecord(int volumeMl, String dataHora, String data) {
        this.volumeMl = volumeMl;
        this.dataHora = dataHora;
        this.data = data;
    }

    public int getVolumeMl() {
        return volumeMl;
    }

    public String getDataHora() {
        return dataHora;
    }

    public String getData() {
        return data;
    }
}
