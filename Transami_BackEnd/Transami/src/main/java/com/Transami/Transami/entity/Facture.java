package com.Transami.Transami.entity;

import com.Transami.Transami.enums.InvoiceStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// com.Transami.Transami.entity.Facture

@Entity
@Table(name = "factures",
       uniqueConstraints = @UniqueConstraint(columnNames = {"numero", "admin_id"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Facture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero", nullable = false)
    private String numero;

    @Column(name = "admin_id", nullable = false)
    private Long adminId;

    @NotNull(message = "La date est obligatoire")
    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(name = "montant_htva", nullable = false, precision = 19, scale = 2)
    private BigDecimal montantHTVA;

    @Column(name = "montant_tva", nullable = false, precision = 19, scale = 2)
    private BigDecimal montantTVA;

    @Column(name = "montant_ttc", nullable = false, precision = 19, scale = 2)
    private BigDecimal montantTTC;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private InvoiceStatus statut = InvoiceStatus.Enattente;

    @NotNull(message = "Les droits de timbre sont obligatoires")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal droitsTimbre;

    // tva field removed

    @OneToMany(mappedBy = "facture", fetch = FetchType.LAZY)
    private List<BonDeLivraison> bonsDeLivraison = new ArrayList<>();
}