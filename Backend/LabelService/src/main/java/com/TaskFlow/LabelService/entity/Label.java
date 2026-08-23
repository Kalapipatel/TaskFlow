package com.TaskFlow.LabelService.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "labels",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_labels_project_name",
                        columnNames = {"project_id", "name"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Label {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(nullable = false, updatable = false, length = 36)
    private UUID id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 7)
    private String color;

    /*
     * Cross-service reference to Project Service.
     * No JPA relationship or foreign key because Project Service owns projects.
     */
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "project_id", nullable = false, updatable = false, length = 36)
    private UUID projectId;

    /*
     * Cross-service reference to User Service.
     * No JPA relationship or foreign key.
     */
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "created_by", nullable = false, updatable = false, length = 36)
    private UUID createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}