// src/main/java/com/example/appprestamos/entidades/PrestamoResumen.java
package com.example.appprestamos.entidades;

public class PrestamoResumen {
    private int totalPrestamos;
    private int idContrato;
    private double monto;
    private double interes;
    private String fechaInicio;
    private int numCuotas;
    private int cuotasPagadas;

    public PrestamoResumen(int totalPrestamos, int idContrato, double monto, double interes, String fechaInicio, int numCuotas, int cuotasPagadas) {
        this.totalPrestamos = totalPrestamos;
        this.idContrato = idContrato;
        this.monto = monto;
        this.interes = interes;
        this.fechaInicio = fechaInicio;
        this.numCuotas = numCuotas;
        this.cuotasPagadas = cuotasPagadas;
    }

    // Getters
    public int getTotalPrestamos() { return totalPrestamos; }
    public int getIdContrato()     { return idContrato; }
    public double getMonto()       { return monto; }
    public double getInteres()     { return interes; }
    public String getFechaInicio() { return fechaInicio; }
    public int getNumCuotas()      { return numCuotas; }
    public int getCuotasPagadas()  { return cuotasPagadas; }
}
