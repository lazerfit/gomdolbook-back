package com.gomdolbook.api.api.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BookCoverDTO {

    private final List<String> covers = new ArrayList<>();

    public void addCovers(String cover) {
        if (cover != null && !covers.contains(cover)) {
            covers.add(cover);
        }
    }
}
