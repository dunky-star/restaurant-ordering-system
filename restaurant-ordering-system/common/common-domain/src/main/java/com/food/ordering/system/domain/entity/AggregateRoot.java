package com.food.ordering.system.domain.entity;

// Marker class
public abstract class AggregateRoot<ID> extends BaseEntity<ID> {
    // AggregateRoot is a type of entity that is the root of an aggregate in Domain-Driven Design (DDD).
    // It is responsible for maintaining the consistency of the aggregate and enforcing business rules.
    // An aggregate is a cluster of domain objects that can be treated as a single unit.
    // The AggregateRoot is the only entity that can be accessed from outside the aggregate.
}