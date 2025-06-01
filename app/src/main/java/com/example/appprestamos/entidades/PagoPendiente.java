package com.example.appprestamos.entidades;

public class PagoPendiente {
    public int numCuota;
    public String fechaVencimiento;
    public double monto;

    public PagoPendiente(int numCuota, String fechaVencimiento, double monto) {
        this.numCuota = numCuota;
        this.fechaVencimiento = fechaVencimiento;
        this.monto = monto;
    }
}
