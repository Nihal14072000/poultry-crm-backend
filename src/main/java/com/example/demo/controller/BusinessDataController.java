package com.example.demo.controller;

import com.example.demo.service.BusinessRecordService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/data")
public class BusinessDataController {
    private final BusinessRecordService service;

    public BusinessDataController(BusinessRecordService service) {
        this.service = service;
    }

    @GetMapping("/{resource}")
    public Map<String, Object> get(@PathVariable String resource) {
        return service.get(resource);
    }

    @PostMapping("/{resource}")
    public ResponseEntity<Map<String, Object>> create(
            @PathVariable String resource, @RequestBody Map<String, Object> body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(resource, body));
    }

    @PutMapping("/{resource}/{id}")
    public Map<String, Object> update(
            @PathVariable String resource, @PathVariable UUID id, @RequestBody Map<String, Object> body) {
        return service.update(resource, id, body);
    }

    @DeleteMapping("/{resource}/{id}")
    public ResponseEntity<Void> delete(@PathVariable String resource, @PathVariable UUID id) {
        service.delete(resource, id);
        return ResponseEntity.noContent().build();
    }
}
