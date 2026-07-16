package com.Transami.Transami.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "invoice_number_counter",
       uniqueConstraints = @UniqueConstraint(columnNames = {"year", "admin_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceNumberCounter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer year;

    @Column(name = "admin_id", nullable = false)
    private Long adminId;

    @Column(nullable = false)
    private Long currentNumber;
}