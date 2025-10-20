package com.example.E_Ticket.controller.api;

import com.example.E_Ticket.dto.VenueDto;
import com.example.E_Ticket.dto.VenueUpsertReq;
import com.example.E_Ticket.exception.NotFoundException;
import com.example.E_Ticket.service.VenueService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/admin/v1/venues")
@RequiredArgsConstructor
public class VenueAdminApi {

    private final VenueService service;

    private static final String VALID_REGEX = "^[\\p{L}0-9\\s.,\\-/()]+$";

    @GetMapping
    public Page<VenueDto> list(@RequestParam(defaultValue="0") int page,
                               @RequestParam(defaultValue="10") int size){
        return service.list(page, size);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            VenueDto dto = service.get(id);
            return ResponseEntity.ok(dto);
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Lỗi: Không tìm thấy địa điểm id=" + id);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi hệ thống: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody VenueUpsertReq r) {

        if (r.name() == null || r.name().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Lỗi: tên địa điểm không được để trống");
        }
        if (!r.name().matches(VALID_REGEX)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Lỗi: tên địa điểm không được chứa ký tự đặc biệt");
        }

        if (r.address() == null || r.address().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Lỗi: địa chỉ không được để trống");
        }
        if (!r.address().matches(VALID_REGEX)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Lỗi: địa chỉ không được chứa ký tự đặc biệt");
        }


        VenueDto created = service.create(r);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody VenueUpsertReq r) {
        try {
            VenueDto exist = service.get(id);
            if (exist == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Lỗi: Không tìm thấy địa điểm id=" + id);
            }

            if (r.name() == null || r.name().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Lỗi: tên địa điểm không được để trống");
            }

            if (r.address() == null || r.address().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Lỗi: địa chỉ không được để trống");
            }
            if (!r.address().matches(VALID_REGEX)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Lỗi: địa chỉ không được chứa ký tự đặc biệt");
            }


            return ResponseEntity.ok(service.update(id, r));
        }catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Lỗi: Không tìm thấy địa điểm id=" + id);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi hệ thống: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            VenueDto exist = service.get(id);
            if (exist == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Lỗi: Không tìm thấy địa điểm id=" + id);
            }
            service.delete(id);
            return ResponseEntity.noContent().build();
        }catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Lỗi: Không tìm thấy địa điểm id=" + id);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi hệ thống: " + e.getMessage());
        }
    }
}