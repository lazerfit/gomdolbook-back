package com.gomdolbook.api.domain.models.readingLog;

import com.gomdolbook.api.domain.models.user.User;
import com.gomdolbook.api.domain.models.book.Book;
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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ReadingLog {

    public enum Status {
        TO_READ,
        READING,
        FINISHED,
        NEW
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "READINGLOG_ID")
    private Long id;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(columnDefinition = "TEXT")
    private String note1;

    @Column(columnDefinition = "TEXT")
    private String note2;

    @Column(columnDefinition = "TEXT")
    private String note3;

    @Column
    private Integer rating;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User user;

    @Setter
    @OneToOne(mappedBy = "readingLog", fetch = FetchType.LAZY)
    private Book book;

    private ReadingLog(Status status) {
        this.status = status;
        note1 = "";
        note2 = "";
        note3 = "";
        rating = 0;
    }

    public void setUser(User user) {
        if (this.user != null) {
            this.user.getReadingLogs().remove(this);
        }
        this.user = user;
        if (user != null && !user.getReadingLogs().contains(this)) {
            user.getReadingLogs().add(this);
        }
    }

    public static ReadingLog of(User user, Status status) {
        ReadingLog readingLog = new ReadingLog(status);
        readingLog.setUser(user);
        return readingLog;
    }

    public void changeStatus(Status status) {
        this.status = status;
    }
    public void changeNote1(String value) {
        this.note1 = value;
    }
    public void changeNote2(String value) {
        this.note2 = value;
    }
    public void changeNote3(String value) {
        this.note3 = value;
    }
    public void changeRating(Integer rating) {
        this.rating = rating;
    }
}
