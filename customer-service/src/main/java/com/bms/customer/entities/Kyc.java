package com.bms.customer.entities;

import com.bms.customer.enums.KycStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
//@Table(
//        name = "kyc_documents",
//        uniqueConstraints = {
//                @UniqueConstraint(
//                        name = "uk_kyc_doc_type_number",
//                        columnNames = {"documentType", "documentNumber"}
//                )
//        }
//)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Kyc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String documentType;

    @Column(nullable = false)
    private String documentNumber;

    @Column(name = "document_url",nullable = false)
    private String documentUrl;    // <-- New

    private String documentFileName; // <-- New

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private KycStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
        if (status == null) {
            status = KycStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}