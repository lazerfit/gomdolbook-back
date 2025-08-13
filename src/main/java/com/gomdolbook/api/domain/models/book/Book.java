package com.gomdolbook.api.domain.models.book;

import com.gomdolbook.api.domain.models.bookmeta.BookMeta;
import com.gomdolbook.api.domain.models.readinglog.ReadingLog;
import com.gomdolbook.api.domain.models.user.User;
import jakarta.persistence.CascadeType;
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
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Book {

    public enum Status {
        TO_READ,
        READING,
        FINISHED,
        NEW
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BOOK_ID")
    private Long id;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column
    private LocalDateTime startedAt;

    @Column
    private LocalDateTime finishedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BOOK_META_ID")
    private BookMeta bookMeta;

    @OneToOne(fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "READINGLOG_ID")
    private ReadingLog readingLog;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User user;

    private void setReadingLog(ReadingLog readingLog) {
        this.readingLog = readingLog;
        if (readingLog != null && readingLog.getBook() != this) {
            readingLog.setBook(this);
        }
    }

    private void setUser(User user) {
        if (this.user != null) {
            this.user.getBooks().remove(this);
        }
        this.user = user;
        if (user != null && !user.getBooks().contains(this)) {
            user.getBooks().add(this);
        }
    }

    private void setBookMeta(BookMeta bookMeta) {
        if (this.bookMeta != null) {
            this.bookMeta.getBooks().remove(this);
        }
        this.bookMeta = bookMeta;
        if (bookMeta != null && !bookMeta.getBooks().contains(this)) {
            bookMeta.getBooks().add(this);
        }
    }

    public static Book of(BookMeta bookMeta, User user) {
        Book book = new Book();
        book.setBookMeta(bookMeta);
        book.setUser(user);
        ReadingLog readingLog = ReadingLog.of();
        book.setReadingLog(readingLog);
        book.status = Status.NEW;
        return book;
    }

    public void changeStatus(Status status) {
        this.status = status;
        if (status == Status.READING) {
            startedAt = LocalDateTime.now();
            finishedAt = null;
        } else if (status == Status.FINISHED) {
            if (startedAt == null) {
                startedAt = LocalDateTime.now();
            }
            finishedAt = LocalDateTime.now();
        }
    }
}
