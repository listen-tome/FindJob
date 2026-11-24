package com.sky.findjob.enums;

public enum ApplicationPriority {
    HIGH("高", 1),
    MEDIUM("中", 2),
    LOW("低", 3);

    private final String description;
    private final int weight; // 数值越小优先级越高

    ApplicationPriority(String description, int weight) {
        this.description = description;
        this.weight = weight;
    }
    public String getDescription() { return description; }
    public int getWeight() { return weight; }
}