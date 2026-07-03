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
public class ItemModel {
    @JsonProperty("id")
    public String id;

    @JsonProperty("item_id")
    public String itemId;

    @JsonProperty("owner_id")
    public String ownerId;

    @JsonProperty("name")
    public String name;

    @JsonProperty("category")
    public String category;

    @JsonProperty("location")
    public String location;

    @JsonProperty("daily_rate")
    public double dailyRate;

    @JsonProperty("available")
    public boolean available;

    @JsonProperty("condition")
    public String condition;

    @JsonProperty("description")
    public String description;

    // Needed for JSON mapping
    public ItemModel() {
    }    
}    

