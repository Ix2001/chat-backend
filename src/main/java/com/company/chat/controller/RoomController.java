package com.company.chat.controller;

import com.company.chat.dto.RoomDto;
import com.company.chat.dto.InviteRequest;
import com.company.chat.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * REST контроллер — комнаты.
 */
@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {
    private final RoomService svc;

    /** GET /api/rooms */
    @GetMapping
    public List<RoomDto> list() {
        return svc.listAll();
    }

    /** POST /api/rooms/direct?u1=&u2= */
    @PostMapping("/direct")
    public RoomDto direct(@RequestParam Long u1, @RequestParam Long u2) {
        return svc.createDirect(u1, u2);
    }

    /** POST /api/rooms/group?name= */
    @PostMapping("/group")
    public RoomDto createGroup(@RequestParam String name, @RequestBody List<Long> users) {
        return svc.createGroup(name, users);
    }

    /** POST /api/rooms/invite */
    @PostMapping("/invite")
    public ResponseEntity<Void> invite(@RequestBody InviteRequest req) {
        svc.inviteUsers(req.getRoomId(), req.getUserIds());
        return ResponseEntity.ok().build();
    }
    @GetMapping("/all-by-user-id")
    public List<RoomDto> byUser(@RequestParam Long userId) {
        return svc.getByUser(userId);
    }
}
