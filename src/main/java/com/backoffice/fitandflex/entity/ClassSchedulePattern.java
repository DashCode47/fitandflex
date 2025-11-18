package com.backoffice.fitandflex.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;
import java.time.LocalDateTime;

@Entity
@Table(name = "class_schedule_patterns")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ClassSchedulePattern {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "day_of_week", nullable = false)
    @EqualsAndHashCode.Include
    private Integer dayOfWeek; // 1=Lunes, 7=Domingo

    @Column(name = "start_time", nullable = false)
    @EqualsAndHashCode.Include
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    @EqualsAndHashCode.Include
    private LocalTime endTime;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean recurrent = false; // Indica si el patrón es recurrente (se repite cada semana)

    // Relación con la clase
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    private Class clazz;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

