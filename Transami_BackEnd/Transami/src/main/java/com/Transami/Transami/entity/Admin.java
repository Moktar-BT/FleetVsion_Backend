package com.Transami.Transami.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import com.Transami.Transami.enums.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import com.Transami.Transami.enums.Language;
import com.Transami.Transami.enums.Theme;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "admins")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Admin implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom est obligatoire")
    @Column(nullable = false)
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    @Column(nullable = false)
    private String prenom;

    @Email(message = "Format email invalide")
    @NotBlank(message = "L'email est obligatoire")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Column(name = "mot_de_passe", nullable = false)
    private String motDePasse;

    @Column(name = "chemin_logo_entreprise")
    private String cheminLogoEntreprise;

    // Remplacement du champ "telephone" par une collection
    @ElementCollection
    @CollectionTable(name = "admin_telephones", joinColumns = @JoinColumn(name = "admin_id"))
    @OrderColumn(name = "telephone_order")
    @Column(name = "telephone")
    private List<String> telephones = new ArrayList<>();

    @Column(nullable = false)
    private String adresse;

    @Column(name = "nom_entreprise", nullable = false)
    private String nomEntreprise;

    @NotBlank(message = "Le matricule fiscale est obligatoire")
    @Column(name = "matricule_fiscale", nullable = false, unique = true)
    private String matriculeFiscale;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Role role = Role.ADMIN;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "language", nullable = false)
    @Builder.Default
    private Language language = Language.FRANCAIS;

    @Enumerated(EnumType.STRING)
    @Column(name = "theme", nullable = false)
    @Builder.Default
    private Theme theme = Theme.LIGHT;

    // ============================================
    // Implémentation UserDetails
    // ============================================

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return this.motDePasse;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.active;
    }
}