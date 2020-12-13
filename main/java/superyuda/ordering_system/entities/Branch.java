package superyuda.ordering_system.entities;

import lombok.*;

import javax.persistence.*;
import java.util.List;
import java.util.Objects;

@ToString
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "branches")
public class Branch implements Comparable<Branch> {

    public Branch(String name, String password) {
        this.name = name;
        this.password = password;
    }
    public Branch(String name, String password,String displayName) {
        this.name = name;
        this.password = password;
        this.displayName = displayName;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", unique = true, nullable = false)
    private String name;

    @Column(name = "dis_name", unique = true, nullable = false)
    private String displayName;

    @Column(name = "password", nullable = false)
    private String password;

    @ToString.Exclude
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "dispatch")
    private List<Order> orders;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Branch branch = (Branch) o;
        return Objects.equals(name, branch.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public int compareTo(Branch o) {
        if (this.equals(o)) {
            return 1;
        }
        return 0;
    }
}
