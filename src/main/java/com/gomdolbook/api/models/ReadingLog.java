package com.gomdolbook.api.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
public class ReadingLog {

    public enum Status {
        TO_READ,
        READING,
        FINISHED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "READINGLOG_ID")
    private Long id;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column
    private String note1;

    @Column
    private String note2;

    @Column
    private String note3;

    @Builder
    public ReadingLog(Status status, String note1, String note2, String note3) {
        this.status = status;
        this.note1 = note1;
        this.note2 = note2;
        this.note3 = note3;
    }
}
