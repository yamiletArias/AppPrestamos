package com.example.appprestamos.entidades;

public class Pago {
    public int numCuota;
    public double monto;
    public boolean pagado;
    public String fechaEstim;
    public double penalidadBD;

    public Pago(int numCuota, double monto, boolean pagado, String fechaEstim, double penalidadBD) {
        this.numCuota = numCuota;
        this.monto = monto;
        this.pagado = pagado;
        this.fechaEstim = fechaEstim;
        this.penalidadBD = penalidadBD;
    }
}