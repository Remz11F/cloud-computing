/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Orchestrator.models;

/**
 *
 * @author N1185866
 */
public class RouteResponseModel {

    public String from;
    public String to;

    public double distanceKm;
    public double durationMins;

    public RouteResponseModel() {
    }

    public RouteResponseModel(String from, String to, double distanceKm, double durationMins) {
        this.from = from;
        this.to = to;
        this.distanceKm = distanceKm;
        this.durationMins = durationMins;
    }
}
