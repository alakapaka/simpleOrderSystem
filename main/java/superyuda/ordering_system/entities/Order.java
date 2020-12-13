package superyuda.ordering_system.entities;

import lombok.*;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "orders")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Order {

    public Order(Long salId, Customer customer, String orderBody, String notes, String orderTime, Branch dispatch) {
        this.salId = salId;
        this.customer = customer;
        this.orderBody = orderBody;
        this.notes = notes;
        this.orderTime = orderTime;
        this.dispatch = dispatch;
    }

    @Id
    @Column(name = "id", unique = true, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sal_id", unique = true, nullable = false)
    private Long salId;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Lob
    @Column(name = "order_body", length = 10000)
    private String orderBody;

    @Lob
    @Column(name = "notes", nullable = false, length = 10000)
    private String notes;

    @JoinColumn(name = "dispatch_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Branch dispatch;


    @Column(name = "order_time", length = 250)
    private String orderTime;

    @Column(name = "pick_up_status")
    private boolean pickUpStatus;

    @Column(name = "charge_status")
    private boolean chargeStatus;

    @Column(name = "sent_status")
    private boolean sentStatus;

    @Column(name = "final_status")
    private boolean finalStatus;

}
