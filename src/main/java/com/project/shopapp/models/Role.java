package com.project.shopapp.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Roles")
@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Role {
  public static final String USER = "USER";
  public static final String ADMIN = "ADMIN";
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "name", nullable = false)
  private String name;

}
