package com.company.chat.controller;

import com.company.chat.dto.CallSessionDto;
import com.company.chat.service.CallService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * REST контроллер — звонки.
 */
@RestController
@RequestMapping("/api/calls")
@RequiredArgsConstructor
public class CallController {
    private final CallService svc;

    /** GET /api/calls */
    @GetMapping
    public List<CallSessionDto> list() {
        return svc.listAll();
    }

}
