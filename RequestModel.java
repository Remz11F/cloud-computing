/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Orchestrator.models;

import com.fasterxml.jackson.annotation.JsonProperty;
/**
 *
 * @author N1185866
 */
public class RequestModel {
    
    @JsonProperty("id")
    public String id;

    @JsonProperty("item_id")
    public String itemId;

    @JsonProperty("user_id")
    public String userId;

    @JsonProperty("status")
    public String status;
    
    public RequestModel() {
    }
}
