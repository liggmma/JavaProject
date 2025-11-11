package luxdine.example.luxdine.domain.catalog.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Categories {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(columnDefinition = "NVARCHAR(128)")
    String name;
    String slug;
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "category_id")
    List<Items> items;
}
