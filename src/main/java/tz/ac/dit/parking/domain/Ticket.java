package tz.ac.dit.parking.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Entity
@Table(name = "ticket")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_code", nullable = false, unique = true, length = 20)
    private String ticketCode;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false, unique = true)
    private ParkingSession session;

    @Column(name = "issued_at", nullable = false)
    private Instant issuedAt;

    protected Ticket() {
    }

    public Ticket(ParkingSession session, Instant issuedAt) {
        this.session = session;
        this.issuedAt = issuedAt;
        this.ticketCode = "PK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
    }

    public Long getId() { return id; }
    public String getTicketCode() { return ticketCode; }
    public ParkingSession getSession() { return session; }
    public Instant getIssuedAt() { return issuedAt; }
}
