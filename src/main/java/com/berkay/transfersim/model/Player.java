package com.berkay.transfersim.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("players")
public class Player {
    @Id
    private String id;

    private String name;
    private int age;
    private String position;     // "GK","DF","MF","FW"
    private double marketValue;  // huidige marktwaarde
    private String club;         // club-name (simpel)

    // getters/setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public double getMarketValue() { return marketValue; }
    public void setMarketValue(double marketValue) { this.marketValue = marketValue; }

    public String getClub() { return club; }
    public void setClub(String club) { this.club = club; }
}
