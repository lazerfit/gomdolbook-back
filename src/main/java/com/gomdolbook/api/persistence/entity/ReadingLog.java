package com.gomdolbook.api.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User user;

    @OneToOne(mappedBy = "readingLog")
    private Book book;

    public ReadingLog(Status status, String note1, String note2, String note3) {
        this.status = status;
        this.note1 = note1;
        this.note2 = note2;
        this.note3 = note3;
    }

    public void setUser(User user) {
        if (this.user != null) {
            this.user.getReadingLogs().remove(this);
        }
        this.user = user;
        user.getReadingLogs().add(this);
    }

    public void updateStatus(Status status) {
        this.status = status;
    }
}
