package com.gomdolbook.api.domain.models.book;

import com.gomdolbook.api.domain.models.bookmeta.BookMeta;
import com.gomdolbook.api.domain.models.readinglog.ReadingLog;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BOOK_ID")
    private Long id;

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

    public void setReadingLog(ReadingLog readingLog) {
        this.readingLog = readingLog;
        if (readingLog != null && readingLog.getBook() != this) {
            readingLog.setBook(this);
        }
    }

    public void deleteReadingLog() {
        this.readingLog = null;
    }

    public static Book of(BookMeta meta) {
        Book book = new Book();
        book.bookMeta = meta;
        if (meta != null) {
            meta.getBooks().add(book);
        }
        return book;
    }

    public void changeStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }
    public void changeFinishedAt(LocalDateTime finishedAt) {
        this.finishedAt = finishedAt;
    }

}
