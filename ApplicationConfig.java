/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Orchestrator;

import java.util.Set;

/**
 *
 * @author N1185866
 */
@javax.ws.rs.ApplicationPath("webresources")
public class ApplicationConfig extends javax.ws.rs.core.Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new java.util.HashSet<>();
        addRestResourceClasses(resources);
        return resources;
    }

    private void addRestResourceClasses(Set<Class<?>> resources) {
        resources.add(Orchestrator.OsrmService.class);
        resources.add(Orchestrator.controllers.ItemController.class);
        resources.add(Orchestrator.controllers.RentalRequestController.class);
        resources.add(Orchestrator.controllers.RouteController.class);
        resources.add(Orchestrator.security.ApiKeyFilter.class);

    }
    
}
