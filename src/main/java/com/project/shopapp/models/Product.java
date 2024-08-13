package com.project.shopapp.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "products")
@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Product extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "name", nullable = false, length = 350)
  private String name;

  private Float price;

  @Column(name = "thumbnail", length = 300)
  private String thumbnail;

  @Column(name = "description")
  private String description;

  @ManyToOne
  @JoinColumn(name = "category_id")
  private Category category;
}
