package luxdine.example.luxdine.domain.catalog.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Allergen {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(columnDefinition = "NVARCHAR(100)", unique = true, nullable = false)
    String name;


    @ManyToMany(mappedBy = "allergens")
    List<Items> items;
}