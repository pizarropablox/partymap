package com.partymap.backend.Controller;



import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.partymap.backend.Service.BffService;



@RestController
@CrossOrigin
@RequestMapping("/bff")
public class BffController {

     private final BffService bffService;

	public BffController(BffService bffService) {
		this.bffService = bffService;
	}

	@PostMapping
	public ResponseEntity<String> create(@RequestBody Map<String, String> body) {
		return ResponseEntity.ok(bffService.create(body));
	}

	@GetMapping("/{id}")
	public ResponseEntity<String> read(@PathVariable("id") String id) {
		return ResponseEntity.ok(bffService.read(id));
	}

	@PutMapping
	public ResponseEntity<String> update(@RequestParam("status") String status) {
		return ResponseEntity.ok(bffService.update(status));
	}

	@DeleteMapping
	public ResponseEntity<String> delete(@RequestHeader("Authorization") String authHeader) {
		return ResponseEntity.ok(bffService.delete(authHeader));
	}

}
