package com.example.appprestamos.entidades;

public class PagoRealizado {
    public int numCuota;
    public String fechaPago;
    public double monto;

    public PagoRealizado(int numCuota, String fechaPago, double monto) {
        this.numCuota = numCuota;
        this.fechaPago = fechaPago;
        this.monto = monto;
    }
}
