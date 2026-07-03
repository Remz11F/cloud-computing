/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Orchestrator;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Maps route details.
 *
 * @author N1185866
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Route {
    public double distance;
    public double duration;
}
