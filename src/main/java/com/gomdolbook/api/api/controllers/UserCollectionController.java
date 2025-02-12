package com.gomdolbook.api.api.controllers;

import com.gomdolbook.api.api.dto.APIResponseDTO;
import com.gomdolbook.api.api.dto.CollectionListResponseDTO;
import com.gomdolbook.api.service.UserCollectionService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@RequiredArgsConstructor
@Controller
public class UserCollectionController {

    private final UserCollectionService userCollectionService;

    @GetMapping("/v1/userCollectionList")
    public ResponseEntity<APIResponseDTO<List<CollectionListResponseDTO>>> getCollectionList() {
        List<CollectionListResponseDTO> collectionList = userCollectionService.getCollectionList();
        if (collectionList.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        APIResponseDTO<List<CollectionListResponseDTO>> response = new APIResponseDTO<>(
            collectionList);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
