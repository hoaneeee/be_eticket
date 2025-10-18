package com.example.E_Ticket.controller.api;


import com.example.E_Ticket.dto.VenueDto;
import com.example.E_Ticket.dto.VenueUpsertReq;
import com.example.E_Ticket.service.VenueService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/v1/venues")
@RequiredArgsConstructor
public class VenueAdminApi
{
    private final VenueService venueService;

    @GetMapping
    public Page<VenueDto> list(@RequestParam(defaultValue="0") int page,
                               @RequestParam(defaultValue="10") int size){
        return venueService.list(page, size);
    }

    @GetMapping("/{id}")
    public VenueDto get(@PathVariable Long id){
        return venueService.get(id);
    }

    @PostMapping
    public VenueDto create(@RequestBody @jakarta.validation.Valid VenueUpsertReq r){
        return venueService.create(r);
    }

    @PutMapping("/{id}")
    public VenueDto update(@PathVariable Long id, @RequestBody @jakarta.validation.Valid VenueUpsertReq r){
        return venueService.update(id, r);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id){
        venueService.delete(id);
    }
}
