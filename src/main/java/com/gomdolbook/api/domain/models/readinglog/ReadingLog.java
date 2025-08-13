package com.gomdolbook.api.domain.models.readinglog;

import com.gomdolbook.api.domain.models.book.Book;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ReadingLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "READINGLOG_ID")
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column
    private Integer rating;

    @Setter
    @OneToOne(mappedBy = "readingLog")
    private Book book;

    public static ReadingLog of() {
        ReadingLog readingLog = new ReadingLog();
        readingLog.changeNote("");
        readingLog.changeSummary("");
        readingLog.changeRating(0);
        return readingLog;
    }

    public void changeSummary(String value) {
        this.summary = value;
    }
    public void changeNote(String value) {
        this.note = value;
    }
    public void changeRating(Integer rating) {
        this.rating = rating;
    }
}
