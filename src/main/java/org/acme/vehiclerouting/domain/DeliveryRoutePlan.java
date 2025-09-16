package org.acme.vehiclerouting.domain;

import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import ai.timefold.solver.core.api.solver.SolverStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

/**
 * The main planning solution class that represents the delivery routing problem.
 * Contains all the data needed for Timefold to solve the problem.
 * 
 * This class holds:
 * - All delivery orders (planning entities)
 * - All available agents (problem facts)
 * - The calculated score
 */
@PlanningSolution
public class DeliveryRoutePlan {

    private String name;

    // Problem facts - these don't change during solving
    @PlanningEntityCollectionProperty
    @ValueRangeProvider(id = "agentRange")
    private List<DeliveryAgent> agents;

    // Planning entities - these will be assigned to agents during solving
    @PlanningEntityCollectionProperty
    private List<DeliveryOrder> orders;

    // The score calculated by the constraint provider
    @PlanningScore
    private HardSoftLongScore score;

    // Solver status for UI tracking
    @JsonIgnore
    private SolverStatus solverStatus;

    // No-arg constructor for Timefold
    public DeliveryRoutePlan() {}

    public DeliveryRoutePlan(String name, List<DeliveryAgent> agents, List<DeliveryOrder> orders) {
        this.name = name;
        this.agents = agents;
        this.orders = orders;
    }

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<DeliveryAgent> getAgents() { return agents; }
    public void setAgents(List<DeliveryAgent> agents) { this.agents = agents; }

    public List<DeliveryOrder> getOrders() { return orders; }
    public void setOrders(List<DeliveryOrder> orders) { this.orders = orders; }

    public HardSoftLongScore getScore() { return score; }
    public void setScore(HardSoftLongScore score) { this.score = score; }

    public SolverStatus getSolverStatus() { return solverStatus; }
    public void setSolverStatus(SolverStatus solverStatus) { this.solverStatus = solverStatus; }

    // Helper methods for analysis

    /**
     * Get the number of unassigned orders.
     */
    @JsonIgnore
    public long getUnassignedOrderCount() {
        return orders.stream()
                .filter(order -> order.getAssignedAgent() == null)
                .count();
    }

    /**
     * Get the number of assigned orders.
     */
    @JsonIgnore
    public long getAssignedOrderCount() {
        return orders.stream()
                .filter(order -> order.getAssignedAgent() != null)
                .count();
    }

    /**
     * Get the total travel time across all agents.
     */
    @JsonIgnore
    public long getTotalTravelTimeSeconds() {
        return agents.stream()
                .mapToLong(DeliveryAgent::getTotalTravelTimeSeconds)
                .sum();
    }

    /**
     * Get the average agent utilization (orders assigned / max capacity).
     */
    @JsonIgnore
    public double getAverageAgentUtilization() {
        if (agents.isEmpty()) return 0.0;

        double totalUtilization = agents.stream()
                .mapToDouble(agent -> (double) agent.getOrderCount() / agent.getMaxOrdersPerShift())
                .sum();

        return totalUtilization / agents.size();
    }

    /**
     * Get statistics about the current solution.
     */
    @JsonIgnore
    public String getSolutionStatistics() {
        return String.format(
            "Solution[%s]: %d/%d orders assigned, %.1f%% agent utilization, total travel: %d min, score: %s",
            name,
            getAssignedOrderCount(),
            orders.size(),
            getAverageAgentUtilization() * 100,
            getTotalTravelTimeSeconds() / 60,
            score != null ? score.toString() : "not calculated"
        );
    }

    @Override
    public String toString() {
        return String.format("DeliveryRoutePlan[%s: %d agents, %d orders, score=%s]", 
                           name, agents.size(), orders.size(), 
                           score != null ? score.toString() : "not calculated");
    }
}