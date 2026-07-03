/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Orchestrator;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * Maps the parts of OSRM JSON
 *
 * @author N1185866
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OsrmResponse {
    public String code;
    public List<Route> routes;
}
