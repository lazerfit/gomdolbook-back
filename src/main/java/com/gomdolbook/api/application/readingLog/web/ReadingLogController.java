package com.gomdolbook.api.application.readingLog.web;

import com.gomdolbook.api.application.readingLog.ReadingLogApplicationService;
import com.gomdolbook.api.application.readingLog.command.ChangeNoteCommand;
import com.gomdolbook.api.application.readingLog.command.ChangeNoteHandler;
import com.gomdolbook.api.application.readingLog.command.ChangeSummaryCommand;
import com.gomdolbook.api.application.readingLog.command.ChangeSummaryHandler;
import com.gomdolbook.api.application.readingLog.command.RatingUpdateCommand;
import com.gomdolbook.api.application.readingLog.command.RatingUpdateHandler;
import com.gomdolbook.api.application.readingLog.dto.ChangeNoteRequestDTO;
import com.gomdolbook.api.application.readingLog.dto.ChangeRatingRequestDTO;
import com.gomdolbook.api.application.readingLog.dto.ChangeSummaryRequestDTO;
import com.gomdolbook.api.application.readingLog.dto.ReadingLogWithBookDTO;
import com.gomdolbook.api.application.shared.ApiResponse;
import com.gomdolbook.api.domain.services.SecurityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class ReadingLogController {

    private final ReadingLogApplicationService readingLogApplicationService;
    private final SecurityService securityService;
    private final RatingUpdateHandler ratingUpdateHandler;
    private final ChangeSummaryHandler changeSummaryHandler;
    private final ChangeNoteHandler changeNoteHandler;


    @GetMapping("/v1/readingLog/{id}")
    public ResponseEntity<ApiResponse<ReadingLogWithBookDTO>> getReadingLog(
        @PathVariable Long id) {
        ReadingLogWithBookDTO readingLog = readingLogApplicationService.getReadingLog(id);
        ApiResponse<ReadingLogWithBookDTO> dto = new ApiResponse<>(
            readingLog);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @PatchMapping("/v1/readingLog/{id}/summary")
    public ResponseEntity<Void> changeSummary(@Valid @RequestBody ChangeSummaryRequestDTO dto,
        @PathVariable Long id) {
        changeSummaryHandler.handle(new ChangeSummaryCommand(dto.summary(), id,
            securityService.getUserEmailFromSecurityContext()));
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/v1/readingLog/{id}/note")
    public ResponseEntity<Void> changeNote(@Valid @RequestBody ChangeNoteRequestDTO dto,
        @PathVariable Long id) {
        changeNoteHandler.handle(new ChangeNoteCommand(dto.note(), id,
            securityService.getUserEmailFromSecurityContext()));
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/v1/readingLog/{id}/rating")
    public ResponseEntity<Void> updateRating(@PathVariable Long id,
        @Valid @RequestBody ChangeRatingRequestDTO dto) {
        RatingUpdateCommand command = new RatingUpdateCommand(id, dto.star());
        ratingUpdateHandler.handle(command);
        return ResponseEntity.ok().build();
    }
}
